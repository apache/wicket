package org.apache.wicket.ng.page.persistent;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.ng.page.ManageablePage;
import org.apache.wicket.ng.page.PageManagerContext;
import org.apache.wicket.ng.page.common.AbstractPageManager;
import org.apache.wicket.ng.page.common.RequestAdapter;

public class PersistentPageManager extends AbstractPageManager
{
    private final PageStore pageStore;
    private final String applicationName;

    public PersistentPageManager(String applicationName, PageStore pageStore)
    {
        this.applicationName = applicationName;
        this.pageStore = pageStore;
        managers.put(applicationName, this);
    }

    private static Map<String, PersistentPageManager> managers = new ConcurrentHashMap<String, PersistentPageManager>();

    /**
     * Represents entry for single session. This is stored as session attribute and caches pages
     * between requests.
     * 
     * @author Matej Knopp
     */
    private static class SessionEntry implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private final String applicationName;
        private final String sessionId;

        public SessionEntry(String applicationName, String sessionId)
        {
            this.applicationName = applicationName;
            this.sessionId = sessionId;
        }

        private PageStore getPageStore()
        {
            PersistentPageManager manager = managers.get(applicationName);
            if (manager == null)
            {
                throw new IllegalStateException("PageManager for application " + applicationName +
                        " not registered.");
            }
            return manager.pageStore;
        }

        /**
         * Add the page to cached pages if page with same id is not already there
         * 
         * @param page
         */
        private void addPage(ManageablePage page)
        {
            if (page != null)
            {
                for (ManageablePage p : pages)
                {
                    if (p.getPageId() == page.getPageId())
                    {
                        return;
                    }
                }
            }
            pages.add(page);
        }

        /**
         * If the pages are stored in temporary state (after deserialization) this method convert
         * them to list of "real" pages
         */
        private void convertAfterReadObjects()
        {
            if (pages == null)
            {
                pages = new ArrayList<ManageablePage>();
            }

            for (Object o : afterReadObject)
            {
                ManageablePage page = getPageStore().convertToPage(o);
                addPage(page);
            }

            afterReadObject = null;
        }

        public synchronized ManageablePage getPage(int id)
        {
            // check if pages are in deserialized state
            if (afterReadObject != null && afterReadObject.isEmpty() == false)
            {
                convertAfterReadObjects();
            }

            // try to find page with same id
            if (pages != null)
            {
                for (ManageablePage page : pages)
                {
                    if (page.getPageId() == id)
                    {
                        return page;
                    }
                }
            }

            // not found, ask pagestore for the page
            return getPageStore().getPage(sessionId, id);
        }

        // set the list of pages to remember after the request
        public synchronized void setPages(List<ManageablePage> pages)
        {
            this.pages = new ArrayList<ManageablePage>(pages);
            afterReadObject = null;
        }

        private transient List<ManageablePage> pages;
        private transient List<Object> afterReadObject;

        private void writeObject(java.io.ObjectOutputStream s) throws IOException
        {
            s.defaultWriteObject();

            // prepare for serialization and store the pages
            List<Serializable> l = new ArrayList<Serializable>();
            for (ManageablePage p : pages)
            {
                l.add(getPageStore().prepareForSerialization(sessionId, p));
            }
            s.writeObject(l);
        }

        @SuppressWarnings("unchecked")
        private void readObject(java.io.ObjectInputStream s) throws IOException,
                ClassNotFoundException
        {
            s.defaultReadObject();

            List<Serializable> l = (List<Serializable>)s.readObject();

            // convert to temporary state after deserialization (will need to be processed
            // by convertAfterReadObject before the pages can be accessed)
            for (Serializable ser : l)
            {
                afterReadObject.add(getPageStore().restoreAfterSerialization(ser));
            }

            afterReadObject = new ArrayList<Object>();
        }
    };

    /**
     * {@link RequestAdapter} for {@link PersistentPageManager}
     * 
     * @author Matej Knopp
     */
    protected class PersitentRequestAdapter extends RequestAdapter
    {
        public PersitentRequestAdapter(PageManagerContext context)
        {
            super(context);
        }

        @Override
        protected ManageablePage getPage(int id)
        {
            // try to get session entry for this session
            SessionEntry entry = getSessionEntry(false);

            if (entry != null)
            {
                return entry.getPage(id);
            }
            else
            {
                return null;
            }
        }

        private static final String ATTRIBUTE_NAME = "wicket:persistentPageManagerData";

        private SessionEntry getSessionEntry(boolean create)
        {
            SessionEntry entry = (SessionEntry)getSessionAttribute(ATTRIBUTE_NAME);
            if (entry == null && create)
            {
                bind();
                entry = new SessionEntry(applicationName, getSessionId());
            }
            if (entry != null)
            {
                synchronized (entry)
                {
                    setSessionAttribute(ATTRIBUTE_NAME, null);
                    setSessionAttribute(ATTRIBUTE_NAME, entry);
                }
            }
            return entry;
        }

        @Override
        protected void newSessionCreated()
        {
            // if the session is not temporary bind a session entry to it
            if (getSessionId() != null)
            {
                getSessionEntry(true);
            }
        }

        @Override
        protected void storeTouchedPages(List<ManageablePage> touchedPages)
        {
            if (!touchedPages.isEmpty())
            {
                SessionEntry entry = getSessionEntry(true);
                entry.setPages(touchedPages);
                for (ManageablePage page : touchedPages)
                {
                    pageStore.storePage(getSessionId(), page);
                }
            }
        }
    };

    @Override
    protected RequestAdapter newRequestAdapter(PageManagerContext context)
    {
        return new PersitentRequestAdapter(context);
    }

    @Override
    public boolean supportsVersioning()
    {
        return true;
    }

    public void sessionExpired(String sessionId)
    {
        pageStore.unbind(sessionId);
    }

    public void destroy()
    {
        managers.remove(applicationName);
    }

}
