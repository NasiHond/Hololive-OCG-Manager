package com.fhict.hololiveocgmanager.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fhict.hololiveocgmanager.entity.ArtEntity;
import com.fhict.hololiveocgmanager.entity.KeywordEntity;
import com.fhict.hololiveocgmanager.entity.TagEntity;

@Service
public class CardScraperService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CardScraperService.class);
    private static final String BASE_URL =
            "https://en.hololive-official-cardgame.com/cardlist/cardsearch_ex?keyword=&attribute%5B0%5D=all&expansion_name=&card_kind%5B0%5D=holomem&rare%5B0%5D=all&bloom_level%5B0%5D=all&parallel%5B0%5D=all&view=image&page=";
    private static final Pattern TRAILING_ART_DAMAGE_PATTERN = Pattern.compile("\\s+[0-9]+(?:[+xX×-])?$");
    private static final Pattern ART_DAMAGE_PATTERN = Pattern.compile("([0-9]+)(?:[+xX×-])?$");

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
                String cardcolour = findDefinitionImageByTerm(infoDetailSection, "Color");
                String hp = findDefinitionByTerm(infoDetailSection, "HP");
                String bloomlvl = findDefinitionByTerm(infoDetailSection, "Bloom Level");
                String batonpass = String.valueOf(countDefinitionImagesByTerm(infoDetailSection, "Baton Pass"));
            String holomem = cardDoc.select(".name").text();
            String image = cardDoc.select(".img.w100 img").attr("src");

            List<ArtEntity> arts = extractArts(cardDoc);

            List<KeywordEntity> keywords = extractKeywords(cardDoc);

            List<TagEntity> tags = extractTags(infoSection);

            //extra
            String extra = extractExtra(cardDoc);

            LOGGER.info("Card scraped: id={}, holomem={}, type={}, rarity={}, set={}, colour={}, hp={}, bloomLevel={}, batonPass={}, image={}, extra={}",
                    cardid, holomem, cardtype, rarity, cardset, cardcolour, hp, bloomlvl, batonpass, image, extra);

            for (int i = 0; i < arts.size(); i++) {
                ArtEntity art = arts.get(i);
                LOGGER.info("Card {} art #{}: cost={}, name={}, damage={}, effect={}",
                    cardid, i + 1, art.getCost(), art.getName(), art.getDamage(), art.getEffect());
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
            art.setCost(extractArtCost(artHeader));
            art.setName(extractArtName(artHeader));
            art.setDamage(extractArtDamage(artHeader));
            art.setEffect(extractArtEffect(artParagraph));
            arts.add(art);
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

    private String extractArtCost(Element artHeader) {

        return artHeader.select("img").eachAttr("alt").stream()
                .map(String::trim)
                .filter(cost -> !cost.isEmpty())
                .reduce("", String::concat);
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