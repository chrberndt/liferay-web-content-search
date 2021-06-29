package com.chberndt.liferay.journal.search;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalContentSearch;
import com.liferay.journal.service.JournalContentSearchLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.IndexerPostProcessor;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.service.LayoutLocalService;

import java.util.List;
import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Christian Berndt
 */
@Component(
	immediate = true,
	property = "indexer.class.name=com.liferay.journal.model.JournalArticle",
	service = IndexerPostProcessor.class
)
public class JournalArticleIndexerPostProcessor
	implements IndexerPostProcessor {

	@Override
	public void postProcessContextBooleanFilter(
		BooleanFilter booleanFilter, SearchContext searchContext) {
	}

	@Override
	public void postProcessDocument(Document document, Object object) {
		JournalArticle article = (JournalArticle)object;

		List<JournalContentSearch> journalContentSearches =
			_journalContentSearchLocalService.getArticleContentSearches(
				article.getGroupId(), article.getArticleId());

		try {
			if (!journalContentSearches.isEmpty()) {
				JournalContentSearch journalContentSearch =
					journalContentSearches.get(0);

				Layout layout = _layoutLocalService.getLayout(
					article.getGroupId(), false,
					journalContentSearch.getLayoutId());

				document.addLocalizedText(
					"localized_layout_name", layout.getNameMap());
				document.addKeyword("layout_uuid", layout.getUuid());
			}
		}
		catch (PortalException pe) {
			if (_log.isErrorEnabled()) {
				_log.error(pe);
			}
		}
	}

	@Override
	public void postProcessFullQuery(
		BooleanQuery fullQuery, SearchContext searchContext) {
	}

	@Override
	public void postProcessSearchQuery(
		BooleanQuery searchQuery, BooleanFilter booleanFilter,
		SearchContext searchContext) {
	}

	@Override
	public void postProcessSummary(
		Summary summary, Document document, Locale locale, String snippet) {
	}

	private static final Log _log = LogFactoryUtil.getLog(
		JournalArticleIndexerPostProcessor.class);

	@Reference
	private JournalContentSearchLocalService _journalContentSearchLocalService;

	@Reference
	private Language _language;

	@Reference
	private LayoutLocalService _layoutLocalService;

}