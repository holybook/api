import {TopBar} from "../common/TopBar";
import {useLoaderData, useSearchParams} from "react-router-dom";
import React, {useEffect, useState} from "react";
import './SearchResult.scss';

export function SearchResultsPage() {
  const supportedLanguages = useLoaderData();
  const [params,] = useSearchParams();
  const query = params.get('q');
  const language = params.get('lang') ?? 'en';

  return (
    <div className="search-results">
      <TopBar
        supportedLanguages={supportedLanguages}
        activeLanguage={language}/>
      <div id="content-container">
        <div id="content">
          <SearchResults
            query={query}
            language={language}/>
        </div>
      </div>
    </div>
  );
}

function SearchResults({query, language}) {
  const [state, setState] = useState({status: 'loading', results: []});

  useEffect(() => {
    if (!query) {
      setState({status: 'empty-query', results: []});
      return;
    }

    let cancelled = false;
    setState({status: 'loading', results: []});

    fetch(`/api/search?q=${encodeURIComponent(query)}&lang=${language}`)
      .then(response => response.json())
      .then(results => {
        if (cancelled) return;
        setState({status: 'done', results});
      })
      .catch(() => {
        if (!cancelled) setState({status: 'error', results: []});
      });

    return () => {
      cancelled = true;
    };
  }, [query, language]);

  if (state.status === 'empty-query') {
    return <p className="search-empty">Type a phrase above to search the writings.</p>;
  }

  if (state.status === 'loading') {
    return <p className="search-empty">Searching…</p>;
  }

  if (state.status === 'error') {
    return <p className="search-empty">Something went wrong. Please try again.</p>;
  }

  return (
    <>
      <header className="search-head">
        <h1 className="search-head__title">
          {state.results.length} {state.results.length === 1 ? 'result' : 'results'}
        </h1>
        <p className="search-head__query">
          for <span>“{query}”</span>
        </p>
      </header>

      {state.results.length === 0
        ? <p className="search-empty">No passages matched your search.</p>
        : state.results.map((result) =>
          <SearchResult
            data={result}
            language={language}
            key={`${result.bookId}:${result.paragraph.index}`}/>
        )}
    </>
  );
}

function SearchResult({data, language}) {
  return (
    <a
      href={`/books/${data.bookId}?lang=${language}&pos=${data.paragraph.index}:80`}
      className="search-result">
      <div
        className="search-result__text"
        dangerouslySetInnerHTML={{__html: data.highlightedText}}/>
      <div className="search-result__cite">
        <span className="search-result__author">{data.author}</span>
        <span className="search-result__sep">·</span>
        <span className="search-result__title">{data.title}</span>
        {data.paragraph.number &&
          <span className="search-result__par">¶ {data.paragraph.number}</span>}
      </div>
    </a>
  );
}
