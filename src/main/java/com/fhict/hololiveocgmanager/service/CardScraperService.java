package com.fhict.hololiveocgmanager.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fhict.hololiveocgmanager.entity.*;
import com.fhict.hololiveocgmanager.repository.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CardScraperService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CardScraperService.class);
    private static final String BASE_URL =
            "https://en.hololive-official-cardgame.com/cardlist/cardsearch_ex?keyword=&attribute%5B0%5D=all&expansion_name=&card_kind%5B0%5D=holomem&rare%5B0%5D=all&bloom_level%5B0%5D=all&parallel%5B0%5D=all&view=image&page=";
    private static final Pattern TRAILING_ART_DAMAGE_PATTERN = Pattern.compile("\\s+[0-9]+(?:[+xX×-])?$");
    private static final Pattern ART_DAMAGE_PATTERN = Pattern.compile("([0-9]+)(?:[+xX×-])?$");

    // Example crit token: "赤+50" (crits on red holomem)
    private static final Pattern CRIT_TOKEN_PATTERN = Pattern.compile("^(赤|黄|青|紫|緑|白|青赤|白緑)\\s*[+＋]\\s*(\\d+)\\s*$");

    /**
     * Cost icons use kanji in the <img alt="...">.
     * We normalize them into stable keys you can store and render from.
     */
    private static final Map<String, String> COST_TOKEN_TO_COLOUR_KEY = Map.of(
            "赤", "red",
            "黄", "yellow",
            "青", "blue",
            "紫", "purple",
            "緑", "green",
            "白", "white",
            "青赤", "blue - red",
            "白緑", "white - green",
            "◇", "colorless"
        );

    private final CardRepository cardRepository;
    private final CardTypeRepository cardTypeRepository;
    private final ColourRepository colourRepository;
    private final ExtraRepository extraRepository;
    private final ArtRepository artRepository;
    private final ArtcostRepository artcostRepository;
    private final CardartRepository cardartRepository;
    private final KeywordRepository keywordRepository;
    private final CardkeywordRepository cardkeywordRepository;
    private final TagRepository tagRepository;
    private final CardtagRepository cardtagRepository;

    public CardScraperService(
            CardRepository cardRepository,
            CardTypeRepository cardTypeRepository,
            ColourRepository colourRepository,
            ExtraRepository extraRepository,
            ArtRepository artRepository,
            ArtcostRepository artcostRepository,
            CardartRepository cardartRepository,
            KeywordRepository keywordRepository,
            CardkeywordRepository cardkeywordRepository,
            TagRepository tagRepository,
            CardtagRepository cardtagRepository
    ) {
        this.cardRepository = cardRepository;
        this.cardTypeRepository = cardTypeRepository;
        this.colourRepository = colourRepository;
        this.extraRepository = extraRepository;
        this.artRepository = artRepository;
        this.artcostRepository = artcostRepository;
        this.cardartRepository = cardartRepository;
        this.keywordRepository = keywordRepository;
        this.cardkeywordRepository = cardkeywordRepository;
        this.tagRepository = tagRepository;
        this.cardtagRepository = cardtagRepository;
    }

    public void scrapeAllCards() {

        int page = 1;
        int totalCards = 0;

        try {

            while (true) {

            LOGGER.info("Scraping card list page {}", page);

                Document doc = Jsoup.connect(BASE_URL + page)
                        .userAgent("Mozilla/5.0")
                        .get();

                Elements cards = doc.select("li a");

                if (cards.isEmpty()) {
                    LOGGER.info("No cards found on page {}, stopping scraper.", page);
                    break;
                }

                LOGGER.info("Found {} card links on page {}", cards.size(), page);

                for (Element card : cards) {

                    String detailUrl = card.absUrl("href");

                    scrapeCardDetails(detailUrl);
                    totalCards++;
                }

                page++;
            }

            LOGGER.info("Finished scraping. Total cards processed: {}", totalCards);

        } catch (IOException e) {
            LOGGER.error("Failed while scraping card list pages.", e);
        }
    }

    private void scrapeCardDetails(String url) {

        try {

            Document cardDoc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get();

                Element infoSection = cardDoc.selectFirst(".info dl, dl.info");
                Element infoDetailSection = cardDoc.selectFirst("dl.info_Detail, dl.info_detail");

            String cardid = cardDoc.select(".number span").text();
                String cardtype = findDefinitionByTerm(infoSection, "Card Type");
                String rarity = findDefinitionByTerm(infoSection, "Rarity");
                String cardset = findDefinitionByTerm(infoSection, "Card Set");
                String hp = findDefinitionByTerm(infoDetailSection, "HP");
                String bloomlvl = findDefinitionByTerm(infoDetailSection, "Bloom Level");
                String batonpass = String.valueOf(countDefinitionImagesByTerm(infoDetailSection, "Baton Pass"));
            String holomem = cardDoc.select(".name").text();

            Element cardImage = cardDoc.selectFirst(".img.w100 img");
            String image = "";
            if (cardImage != null) {
                image = normalizeText(cardImage.absUrl("src"));
                if (image.isEmpty()) {
                    image = normalizeText(cardImage.attr("src"));
                }
            }

            List<ArtEntity> arts = extractArts(cardDoc);

            List<KeywordEntity> keywords = extractKeywords(cardDoc);

            List<TagEntity> tags = extractTags(infoSection);

            String extra = extractExtra(cardDoc);

                // Persist scraped card and all related data.
                persistCard(cardid, cardset, cardtype, rarity, hp, bloomlvl, batonpass, holomem, image, extra, infoDetailSection, infoSection, cardDoc);

            LOGGER.info("Card scraped: id={}, holomem={}, type={}, rarity={}, set={}, colour={}, hp={}, bloomLevel={}, batonPass={}, image={}, extra={}",
                    cardid, holomem, cardtype, rarity, cardset, extractCardColourDebug(infoDetailSection), hp, bloomlvl, batonpass, image, extra);

            for (int i = 0; i < arts.size(); i++) {
                ArtEntity art = arts.get(i);
                LOGGER.info("Card {} art #{}: name={}, damage={}, effect={}, critColour={}"
                    , cardid, i + 1, art.getName(), art.getDamage(), art.getEffect(),
                        art.getCritColour() == null ? null : art.getCritColour().getColour());
            }

            for (int i = 0; i < keywords.size(); i++) {
                KeywordEntity keyword = keywords.get(i);
                LOGGER.info("Card {} keyword #{}: type={}, title={}, effect={}",
                        cardid, i + 1, keyword.getType(), keyword.getName(), keyword.getEffect());
            }

            for (int i = 0; i < tags.size(); i++) {
                TagEntity tag = tags.get(i);
                LOGGER.info("Card {} tag #{}: name={}", cardid, i + 1, tag.getName());
            }

        } catch (IOException e) {
            LOGGER.error("Failed to scrape card details from {}", url, e);
        }
    }

    private void persistCard(
            String rawCardId,
            String scrapedCardSet,
            String scrapedCardType,
            String rarity,
            String hp,
            String bloomlvl,
            String batonpass,
            String holomem,
            String image,
            String extraEffect,
            Element infoDetailSection,
            Element infoSection,
            Document cardDoc
    ) {

        String normalizedCardId = normalizeText(rawCardId);
        String derivedSet = deriveSetCode(normalizedCardId);
        String cardSet = !normalizeText(scrapedCardSet).isEmpty() ? normalizeText(scrapedCardSet) : derivedSet;

        if (normalizedCardId.isEmpty() || cardSet.isEmpty()) {
            LOGGER.warn("Skipping card persist: rawCardId='{}' scrapedCardSet='{}' (cannot derive cardId/cardSet)", rawCardId, scrapedCardSet);
            return;
        }

        String normalizedRarity = normalizeText(rarity);

        if (cardRepository.findByCardidIgnoreCaseAndCardsetIgnoreCaseAndRarityIgnoreCase(normalizedCardId, cardSet, normalizedRarity).isPresent()) {
            LOGGER.info("Card already exists in DB ({} / {}), skipping persist.", cardSet, normalizedCardId);
            return;
        }

        Integer hpValue = parseInteger(hp);
        if (hpValue == null) {
            LOGGER.warn("Skipping card persist: rawCardId='{}' has non-numeric HP='{}'", rawCardId, hp);
            return;
        }

        Optional<ColourToken> cardColourToken = extractCardColourToken(infoDetailSection);
        if (cardColourToken.isEmpty()) {
            LOGGER.warn("Skipping card persist: rawCardId='{}' has no detectable card colour", rawCardId);
            return;
        }

        CardtypeEntity resolvedType = resolveCardType(scrapedCardType);
        ColourEntity resolvedCardColour = resolveColourReference(cardColourToken.get().colourKey, Optional.ofNullable(cardColourToken.get().imageUrl));
        ExtraEntity resolvedExtra = resolveExtra(extraEffect);

        if (resolvedType == null || resolvedCardColour == null) {
            LOGGER.warn("Skipping card persist: rawCardId='{}' missing type/colour (type='{}', colour='{}')", rawCardId, scrapedCardType, cardColourToken.get().colourKey);
            return;
        }

        CardEntity cardEntity = new CardEntity();
        cardEntity.setCardid(normalizedCardId);
        cardEntity.setCardset(cardSet);
        cardEntity.setCardtype(resolvedType);
        cardEntity.setCardcolour(resolvedCardColour);
        cardEntity.setBatonpass(normalizeText(batonpass));
        cardEntity.setHolomem(normalizeText(holomem));
        cardEntity.setBloomlvl(normalizeText(bloomlvl));
        cardEntity.setHp(hpValue);
        cardEntity.setRarity(normalizedRarity);
        cardEntity.setImage(normalizeText(image));
        cardEntity.setExtra(resolvedExtra);

        CardEntity savedCard = cardRepository.save(cardEntity);

        persistArts(savedCard, cardDoc);
        persistKeywords(savedCard, cardDoc);
        persistTags(savedCard, infoSection);
    }

    private void persistArts(CardEntity savedCard, Document cardDoc) {

        Elements artParagraphs = cardDoc.select(".arts p");

        for (Element artParagraph : artParagraphs) {

            Element artHeader = artParagraph.selectFirst("span");
            if (artHeader == null) {
                continue;
            }

            ArtEntity art = new ArtEntity();
            resolveArtCritColour(art, artHeader);
            art.setName(extractArtName(artHeader));
            art.setDamage(extractArtDamage(artHeader));
            art.setEffect(extractArtEffect(artParagraph));

            ArtEntity savedArt = artRepository.save(art);

            CardartEntity link = new CardartEntity();
            link.setCardid(savedCard);
            link.setArtid(savedArt);
            cardartRepository.save(link);

            Map<String, Integer> costCounts = extractArtCostColourKeyCounts(artHeader);
            Map<String, String> costImageUrls = extractArtCostColourKeyImageUrls(artHeader);

            List<ArtcostEntity> costs = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : costCounts.entrySet()) {
                ArtcostEntity artcostEntity = new ArtcostEntity();
                artcostEntity.setArt(savedArt);
                artcostEntity.setColour(resolveColourReference(
                        entry.getKey(),
                        Optional.ofNullable(costImageUrls.get(entry.getKey()))
                ));
                artcostEntity.setAmount(entry.getValue());
                costs.add(artcostEntity);
            }

            artcostRepository.saveAll(costs);
        }
    }

    private void persistKeywords(CardEntity savedCard, Document cardDoc) {

        for (KeywordEntity scraped : extractKeywords(cardDoc)) {

            String type = normalizeText(scraped.getType());
            String name = normalizeText(scraped.getName());
            String effect = normalizeText(scraped.getEffect());

            if (type.isEmpty() && name.isEmpty() && effect.isEmpty()) {
                continue;
            }

            KeywordEntity resolved = keywordRepository
                    .findByTypeIgnoreCaseAndNameIgnoreCaseAndEffectIgnoreCase(type, name, effect)
                    .orElseGet(() -> keywordRepository.save(KeywordEntity.builder()
                            .type(type)
                            .name(name)
                            .effect(effect)
                            .build()));

            CardkeywordEntity link = new CardkeywordEntity();
            link.setCardid(savedCard);
            link.setKeywordid(resolved);
            cardkeywordRepository.save(link);
        }
    }

    private void persistTags(CardEntity savedCard, Element infoSection) {

        for (TagEntity scraped : extractTags(infoSection)) {
            String name = normalizeText(scraped.getName());
            if (name.isEmpty()) {
                continue;
            }

            TagEntity resolved = tagRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> tagRepository.save(TagEntity.builder()
                            .name(name)
                            .build()));

            CardtagEntity link = new CardtagEntity();
            link.setCardid(savedCard);
            link.setTagid(resolved);
            cardtagRepository.save(link);
        }
    }

    private CardtypeEntity resolveCardType(String typeName) {

        String normalized = normalizeText(typeName);
        if (normalized.isEmpty()) {
            return null;
        }

        return cardTypeRepository.findByNameIgnoreCase(normalized)
                .orElseGet(() -> cardTypeRepository.save(CardtypeEntity.builder()
                        .name(normalized)
                        .build()));
    }

    private ExtraEntity resolveExtra(String extraEffect) {

        String normalized = normalizeText(extraEffect);
        if (normalized.isEmpty()) {
            return null;
        }

        return extraRepository.findByEffectIgnoreCase(normalized)
                .orElseGet(() -> extraRepository.save(ExtraEntity.builder()
                        .effect(normalized)
                        .build()));
    }

    private Integer parseInteger(String raw) {
        String normalized = normalizeText(raw);
        if (normalized.isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String deriveSetCode(String rawCardId) {
        String normalized = normalizeText(rawCardId);
        int dash = normalized.indexOf('-');
        if (dash <= 0) {
            return "";
        }
        return normalizeText(normalized.substring(0, dash));
    }

    private record ColourToken(String colourKey, String imageUrl) {}

    private Optional<ColourToken> extractCardColourToken(Element infoDetailSection) {

        Element dd = findDefinitionElement(infoDetailSection, "Color");
        if (dd == null) {
            return Optional.empty();
        }

        Element img = dd.selectFirst("img[alt]");
        if (img == null) {
            return Optional.empty();
        }

        String alt = normalizeText(img.attr("alt"));
        Optional<String> key = normalizeCostTokenToColourKey(alt);
        if (key.isEmpty()) {
            return Optional.empty();
        }

        String src = normalizeText(img.absUrl("src"));
        if (src.isEmpty()) {
            src = normalizeText(img.attr("src"));
        }

        return Optional.of(new ColourToken(key.get(), src));
    }

    private String extractCardColourDebug(Element infoDetailSection) {
        return extractCardColourToken(infoDetailSection)
                .map(token -> token.colourKey + " (" + token.imageUrl + ")")
                .orElse("");
    }

    private List<KeywordEntity> extractKeywords(Document cardDoc) {

        List<KeywordEntity> keywords = new ArrayList<>();
        Elements keywordBlocks = cardDoc.select(".keyword");

        for (Element keywordBlock : keywordBlocks) {

            Element keywordParagraph = keywordBlock.selectFirst("p:nth-of-type(2)");

            if (keywordParagraph == null) {
                continue;
            }

            Element keywordSpan = keywordParagraph.selectFirst("span");

            if (keywordSpan == null) {
                continue;
            }

            KeywordEntity keyword = new KeywordEntity();
            keyword.setType(normalizeText(keywordSpan.select("img").attr("alt")));
            keyword.setName(extractKeywordName(keywordSpan));
            keyword.setEffect(extractKeywordEffect(keywordParagraph));
            keywords.add(keyword);
        }

        return keywords;
    }

    private List<ArtEntity> extractArts(Document cardDoc) {

        List<ArtEntity> arts = new ArrayList<>();
        Elements artParagraphs = cardDoc.select(".arts p");

        for (Element artParagraph : artParagraphs) {

            Element artHeader = artParagraph.selectFirst("span");

            if (artHeader == null) {
                continue;
            }

            ArtEntity art = new ArtEntity();
            resolveArtCritColour(art, artHeader);
            art.setName(extractArtName(artHeader));
            art.setDamage(extractArtDamage(artHeader));
            art.setEffect(extractArtEffect(artParagraph));
            arts.add(art);

            Map<String, Integer> costCounts = extractArtCostColourKeyCounts(artHeader);
            Map<String, String> costImageUrls = extractArtCostColourKeyImageUrls(artHeader);
            for (Map.Entry<String, Integer> entry : costCounts.entrySet())
            {
                ArtcostEntity artcostEntity = new ArtcostEntity();
                artcostEntity.setArt(art);
                artcostEntity.setColour(resolveColourReference(
                        entry.getKey(),
                        Optional.ofNullable(costImageUrls.get(entry.getKey()))
                ));
                artcostEntity.setAmount(entry.getValue());
            }
        }

        return arts;
    }

    private List<TagEntity> extractTags(Element infoSection) {

        List<TagEntity> tags = new ArrayList<>();
        Element tagDefinition = findDefinitionElement(infoSection, "Tag");

        if (tagDefinition == null) {
            return tags;
        }

        Elements tagAnchors = tagDefinition.select("a");

        for (Element tagAnchor : tagAnchors) {

            String tagName = normalizeText(tagAnchor.text());

            if (tagName.isEmpty()) {
                continue;
            }

            TagEntity tag = new TagEntity();
            tag.setName(tagName);
            tags.add(tag);
        }

        // Fallback for rare pages where tags are plain text instead of anchor links.
        if (!tags.isEmpty()) {
            return tags;
        }

        String rawTags = normalizeText(tagDefinition.text());

        if (rawTags.isEmpty()) {
            return tags;
        }

        for (String rawTag : rawTags.split("\\s{2,}|,")) {

            String tagName = normalizeText(rawTag);

            if (tagName.isEmpty()) {
                continue;
            }

            TagEntity tag = new TagEntity();
            tag.setName(tagName);
            tags.add(tag);
        }

        return tags;
    }

    @SuppressWarnings("unused")
    private String extractArtCost(Element artHeader) {

        // Keep the current `String` representation for now, but build it from tokens.
        // This avoids losing boundaries if you later want to persist cost components.
        return String.join("", extractArtCostColourKeys(artHeader));
    }

    /**
     * Extract each cost icon as a discrete token (one <img alt="..."> == one token).
     * Example: "◇" -> ["◇"], "R R B" -> ["R", "R", "B"].
     */
    private List<String> extractArtCostTokens(Element artHeader) {

        if (artHeader == null) {
            return List.of();
        }

        return artHeader.select("img[alt]").eachAttr("alt").stream()
                .map(this::normalizeText)
                .filter(token -> !token.isEmpty())
                .toList();
    }

    /**
     * Convert cost tokens (e.g. "赤", "◇") into canonical keys (e.g. "red", "colorless").
     * This is what you typically want to persist as a FK to `colours`.
     */
    private List<String> extractArtCostColourKeys(Element artHeader) {

        return extractArtCostTokens(artHeader).stream()
                .map(this::normalizeCostTokenToColourKey)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * Aggregate canonical keys into counts.
     * Example: ["red", "red", "blue"] -> {"red"=2, "blue"=1}
     */
    private Map<String, Integer> extractArtCostColourKeyCounts(Element artHeader) {

        Map<String, Integer> counts = new LinkedHashMap<>();

        for (String key : extractArtCostColourKeys(artHeader)) {
            counts.merge(key, 1, (existing, add) -> existing + add);
        }

        return counts;
    }

    /**
     * Maps a scraped token into a stable colour key.
     * Returns empty for unknown tokens so the scraper can continue.
     */
    private Optional<String> normalizeCostTokenToColourKey(String token) {

        String normalized = normalizeText(token);

        if (normalized.isEmpty()) {
            return Optional.empty();
        }

        // Tokens like "赤+50" are crit indicators, not part of the cost.
        if (CRIT_TOKEN_PATTERN.matcher(normalized).matches()) {
            return Optional.empty();
        }

        String key = COST_TOKEN_TO_COLOUR_KEY.get(normalized);

        if (key == null) {
            LOGGER.warn("Unknown art cost token '{}' (not in COST_TOKEN_TO_COLOUR_KEY)", normalized);
            return Optional.empty();
        }

        return Optional.of(key);
    }

    private void resolveArtCritColour(ArtEntity art, Element artHeader) {

        if (art == null) {
            return;
        }

        Optional<String> critColourKey = extractArtCritColourKey(artHeader);

        if (critColourKey.isEmpty()) {
            art.setCritColour(null);
            return;
        }

        ColourEntity resolved = resolveColourReference(critColourKey.get(), Optional.empty());
        art.setCritColour(resolved);
    }

    /**
     * Extract crit colour from tokens like "赤+50".
     * Stores only the colour (the "+50" amount is currently ignored).
     */
    private Optional<String> extractArtCritColourKey(Element artHeader) {

        for (String token : extractArtCostTokens(artHeader)) {
            Optional<String> key = normalizeCritTokenToColourKey(token);
            if (key.isPresent()) {
                return key;
            }
        }

        return Optional.empty();
    }

    private Optional<String> normalizeCritTokenToColourKey(String token) {

        String normalized = normalizeText(token);

        if (normalized.isEmpty()) {
            return Optional.empty();
        }

        Matcher matcher = CRIT_TOKEN_PATTERN.matcher(normalized);

        if (!matcher.matches()) {
            return Optional.empty();
        }

        String colourToken = matcher.group(1);
        String colourKey = COST_TOKEN_TO_COLOUR_KEY.get(colourToken);

        if (colourKey == null) {
            LOGGER.warn("Crit token '{}' matched CRIT_TOKEN_PATTERN but colour token '{}' is unmapped", normalized, colourToken);
            return Optional.empty();
        }

        return Optional.of(colourKey);
    }

    private ColourEntity resolveColourReference(String colourKey, Optional<String> url) {

        if (colourKey == null || colourKey.isBlank()) {
            return null;
        }

        String name = colourKey.trim();

        ColourEntity resolved = colourRepository.findByColourIgnoreCase(name)
                .orElseGet(() -> colourRepository.save(ColourEntity.builder()
                        .colour(name)
                        .imageUrl(url.orElse(null))
                        .build()));

        // If the colour already existed but had no imageUrl yet, fill it in.
        if (resolved.getImageUrl() == null && url != null && url.isPresent() && !url.get().isBlank()) {
            resolved.setImageUrl(url.get().trim());
            resolved = colourRepository.save(resolved);
        }

        return resolved;
    }

    /**
     * Extract image URLs for cost icons, keyed by canonical colour key.
     * Example: <img alt="赤" src="/icons/red.png"> -> {"red"="https://.../icons/red.png"}
     */
    private Map<String, String> extractArtCostColourKeyImageUrls(Element artHeader) {

        Map<String, String> urls = new LinkedHashMap<>();

        if (artHeader == null) {
            return urls;
        }

        for (Element img : artHeader.select("img[alt][src]")) {
            String alt = normalizeText(img.attr("alt"));
            Optional<String> key = normalizeCostTokenToColourKey(alt);

            if (key.isEmpty()) {
                continue;
            }

            String src = normalizeText(img.absUrl("src"));
            if (src.isEmpty()) {
                src = normalizeText(img.attr("src"));
            }

            if (src.isEmpty()) {
                continue;
            }

            // Keep the first-seen url for a given colour key.
            urls.putIfAbsent(key.get(), src);
        }

        return urls;
    }

    /**
     * Aggregate tokens into counts while keeping first-seen order.
     * Example: ["R", "R", "B"] -> {"R"=2, "B"=1}
     */
    @SuppressWarnings("unused")
    private Map<String, Integer> extractArtCostTokenCounts(Element artHeader) {

        Map<String, Integer> counts = new LinkedHashMap<>();

        for (String token : extractArtCostTokens(artHeader)) {
            counts.merge(token, 1, (existing, add) -> existing + add);
        }

        return counts;
    }

    private String extractArtName(Element artHeader) {

        Element artHeaderClone = artHeader.clone();
        artHeaderClone.select("img, .tokkou").remove();

        String artNameWithDamage = artHeaderClone.text().replace('\u3000', ' ').trim();

        return TRAILING_ART_DAMAGE_PATTERN.matcher(artNameWithDamage).replaceFirst("").trim();
    }

    private Integer extractArtDamage(Element artHeader) {

        Element artHeaderClone = artHeader.clone();
        artHeaderClone.select("img, .tokkou").remove();

        String artNameWithDamage = normalizeText(artHeaderClone.text());
        java.util.regex.Matcher damageMatcher = ART_DAMAGE_PATTERN.matcher(artNameWithDamage);

        if (!damageMatcher.find()) {
            return null;
        }

        return Integer.parseInt(damageMatcher.group(1));
    }

    private String extractArtEffect(Element artParagraph) {

        Element artParagraphClone = artParagraph.clone();
        artParagraphClone.select("span").remove();

        return normalizeText(artParagraphClone.text());
    }

    private String extractKeywordName(Element keywordSpan) {

        Element keywordSpanClone = keywordSpan.clone();
        keywordSpanClone.select("img").remove();

        return normalizeText(keywordSpanClone.text());
    }

    private String extractKeywordEffect(Element keywordParagraph) {

        Element keywordParagraphClone = keywordParagraph.clone();
        keywordParagraphClone.select("span").remove();

        return normalizeText(keywordParagraphClone.text());
    }

    private String extractExtra(Document cardDoc) {

        Element extraParagraph = cardDoc.selectFirst(".extra p:nth-of-type(2)");

        if (extraParagraph == null) {
            return "";
        }

        Element extraClone = extraParagraph.clone();
        extraClone.select("span").remove();

        return normalizeText(extraClone.text());
    }

    private String findDefinitionByTerm(Element definitionList, String termLabel) {

        Element definitionValue = findDefinitionElement(definitionList, termLabel);
        return definitionValue == null ? "" : normalizeText(definitionValue.text());
    }

    @SuppressWarnings("unused")
    private String findDefinitionImageByTerm(Element definitionList, String termLabel) {

        Element definitionValue = findDefinitionElement(definitionList, termLabel);

        if (definitionValue == null) {
            return "";
        }

        String imageSource = definitionValue.select("img").attr("src");
        return normalizeText(imageSource);
    }

    private int countDefinitionImagesByTerm(Element definitionList, String termLabel) {

        Element definitionValue = findDefinitionElement(definitionList, termLabel);
        return definitionValue == null ? 0 : definitionValue.select("img").size();
    }

    private Element findDefinitionElement(Element definitionList, String termLabel) {

        if (definitionList == null) {
            return null;
        }

        Elements terms = definitionList.select("dt");

        for (Element term : terms) {

            if (!termLabel.equalsIgnoreCase(normalizeText(term.text()))) {
                continue;
            }

            return term.nextElementSibling();
        }

        return null;
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.replace('\u3000', ' ').trim();
    }
}