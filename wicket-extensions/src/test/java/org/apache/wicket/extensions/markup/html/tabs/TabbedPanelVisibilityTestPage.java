package org.apache.wicket.extensions.markup.html.tabs;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 */
public class TabbedPanelVisibilityTestPage extends WebPage {

    final TabbedPanel tabbedPanel;

    public TabbedPanelVisibilityTestPage(int nbTabs, int nbTabsVisible) {
        List<ITab> tabs = new ArrayList<ITab>(nbTabs);
        for (int i = 0; i < nbTabs; i++){
            tabs.add(new DummyTab(i < nbTabsVisible));
        }

        tabbedPanel = new TabbedPanel("tabbedPanel", tabs);
        add(tabbedPanel);
    }

    public static final class DummyTab implements ITab{
        private boolean visible;

        public DummyTab(final boolean visible) {
            this.visible = visible;
        }

        @Override
        public IModel<String> getTitle() {
            return Model.of("Dummy");
        }

        @Override
        public WebMarkupContainer getPanel(final String containerId) {
            return new EmptyPanel(containerId);
        }

        @Override
        public boolean isVisible() {
            return visible;
        }
    }
}
