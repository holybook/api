import {TopBar} from '../common/TopBar';
import {useLoaderData, useSearchParams} from 'react-router-dom';
import './Overview.scss';
import {AuthorBookItems} from './AuthorBookItems';
import React, {useState} from "react";

export function Overview() {
  const {supportedLanguages, books} = useLoaderData();
  const [params,] = useSearchParams();
  const language = params.get('lang') ?? 'en';

  return (
    <div className="overview">
      <TopBar supportedLanguages={supportedLanguages}
              activeLanguage={language}/>
      <div id="content-container">
        <div id="content">
          {Object.keys(books).map(author =>
            <AuthorBookItems author={author} books={books[author]}/>
          )}
        </div>
      </div>
    </div>
  );
}