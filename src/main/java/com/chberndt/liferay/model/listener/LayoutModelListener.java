package com.chberndt.liferay.model.listener;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalContentSearch;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.journal.service.JournalContentSearchLocalService;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchException;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Christian Berndt
 */
@Component(immediate = true, service = ModelListener.class)
public class LayoutModelListener extends BaseModelListener<Layout> {

	@Override
	public void onAfterUpdate(Layout layout) throws ModelListenerException {
		try {
			_reindexJournalArticles(
				layout.getCompanyId(), layout.getGroupId(),
				layout.getLayoutId());
		}
		catch (PortalException pe) {
			System.err.println(pe);

			throw new ModelListenerException(pe);
		}

		super.onAfterUpdate(layout);
	}

	private void _reindexJournalArticle(JournalArticle journalArticle) {
		try {
			Indexer<JournalArticle> indexer =
				IndexerRegistryUtil.nullSafeGetIndexer(JournalArticle.class);

			indexer.reindex(journalArticle);
		}
		catch (SearchException searchException) {
			throw new ModelListenerException(searchException);
		}
	}

	private void _reindexJournalArticles(
			long companyId, long groupId, long layoutId)
		throws PortalException {

		DynamicQuery dynamicQuery =
			_journalContentSearchLocalService.dynamicQuery();

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq("companyId", companyId)
		).add(
			RestrictionsFactoryUtil.eq("groupId", groupId)
		).add(
			RestrictionsFactoryUtil.eq("layoutId", layoutId)
		);

		List<JournalContentSearch> journalContentSearches =
			_journalContentSearchLocalService.dynamicQuery(dynamicQuery);

		for (JournalContentSearch journalContentSearch :
				journalContentSearches) {

			JournalArticle article = _journalArticleLocalService.getArticle(
				groupId, journalContentSearch.getArticleId());

			_reindexJournalArticle(article);
		}
	}

	@Reference
	private JournalArticleLocalService _journalArticleLocalService;

	@Reference
	private JournalContentSearchLocalService _journalContentSearchLocalService;

}