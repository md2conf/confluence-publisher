package io.github.md2conf.converter.noop;

import io.github.md2conf.converter.AttachmentUtil;
import io.github.md2conf.converter.Converter;
import io.github.md2conf.indexer.Page;
import io.github.md2conf.indexer.PagesStructure;
import io.github.md2conf.model.ConfluenceContentModel;
import io.github.md2conf.model.ConfluencePage;
import io.github.md2conf.title.processor.PageStructureTitleProcessor;
import io.github.md2conf.title.processor.wiki.WikiHeaderRemover;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoopConverter implements Converter {

    private final PageStructureTitleProcessor pageStructureTitleProcessor;
    private final boolean needToRemoveHeaderWithTitle;

    public NoopConverter(PageStructureTitleProcessor pageStructureTitleProcessor, boolean needToRemoveHeaderWithTitle) {
        this.pageStructureTitleProcessor = pageStructureTitleProcessor;
        this.needToRemoveHeaderWithTitle = needToRemoveHeaderWithTitle;
    }


    @Override
    public ConfluenceContentModel convert(PagesStructure pagesStructure) throws IOException {
        Map<Path,String> titleMap = pageStructureTitleProcessor.toTitleMap(pagesStructure);

        List<ConfluencePage> confluencePages = new ArrayList<>();
        for (Page topLevelPage : pagesStructure.pages()) { //use "for" loop to throw exception to caller
            ConfluencePage confluencePage = createConfluencePage(topLevelPage, titleMap);
            confluencePages.add(confluencePage);
        }
        return new ConfluenceContentModel(confluencePages);
    }

    private ConfluencePage createConfluencePage(Page page, Map<Path, String> titleMap) {
        ConfluencePage result = new ConfluencePage();
        result.setContentFilePath(page.path().toString());
        result.setTitle(titleMap.get(page.path().toAbsolutePath()));
        result.setAttachments(AttachmentUtil.toAttachmentsMap(page.attachments()));
        for (Page childPage : page.children()) {
            ConfluencePage childConfluencePage = createConfluencePage(childPage, titleMap);
            result.getChildren().add(childConfluencePage);
        }
        if (needToRemoveHeaderWithTitle){
            WikiHeaderRemover.removeFirstHeader(page.path());
        }
        return result;
    }
}
