import {TopBar} from "../common/TopBar";
import {useLoaderData, useSearchParams} from "react-router-dom";
import React, {useState} from "react";
import './SearchResult.scss';

export function SearchResultsPage() {
  const supportedLanguages = useLoaderData();
  const [params,] = useSearchParams();
  const query = params.get('q');
  const language = params.get('lang') ?? 'en';

  return (<div className="search-results">
    <TopBar
      supportedLanguages={supportedLanguages}
      activeLanguage={language}/>
    <div id="content">
      <SearchResults
        query={query}
        language={language}/>
    </div>
  </div>);
}

function SearchResults({query, language}) {
  const [state, setState] = useState(null);

  function fetchSearchResults(query, language) {
    fetch(
      `/api/search?q=${query}&lang=${language}`)
      .then(response => {
        return response.json();
      })
      .then(results => {
        setState({
          results: results,
          language: language,
          query: query
        });
      });
  }

  if (state === null || query !== state.query || language !== state.language) {
    fetchSearchResults(query, language)
  }

  if (state === null) {
    return <div>
      Loading...
    </div>
  }

  return state.results.map((result) =>
    <SearchResult
      data={result}
      language={language}
      key={`${result.bookId}:${result.paragraph.index}`}
    />
  )
}

function SearchResult({data, language}) {
  return (
    <a href={`/books/${data.bookId}?lang=${language}&pos=${data.paragraph.index}:80`}
       className="search-result">
      <div
        dangerouslySetInnerHTML={{
          __html: data.highlightedText
        }}/>
      <div className="search-result-author">
        <span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
        &mdash; {data.author}, {data.title}, par. {data.paragraph.number}
      </div>
    </a>
  )
}