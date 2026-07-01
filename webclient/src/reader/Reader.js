import {useLoaderData, useSearchParams} from "react-router-dom";
import {Paragraphs} from "./Paragraphs";
import {TableOfContents} from "./TableOfContents";

import '@fontsource/roboto/300.css';
import '@fontsource/roboto/400.css';
import '@fontsource/roboto/500.css';
import '@fontsource/roboto/700.css';
import {TopBar} from "../common/TopBar";
import {parsePosition} from "./ScrollPosition";
import './Reader.scss';

export function Reader() {
  const {book, paragraphs} = useLoaderData();
  const [params,] = useSearchParams();
  const encodedPosition = params.get('pos');
  const position = (encodedPosition !== null) ? parsePosition(encodedPosition)
      : null;
  const language = params.get('lang') ?? 'en';

  const supportedLanguages = book.translations.map(
      (translation) => translation.language);

  const translation = book.translations.find(
      (t) => t.language === language) ?? book.translations[0];

  return (
      <div className="reader">
        <TopBar
            book={book}
            activeLanguage={language}
            supportedLanguages={supportedLanguages}/>
        <div className="reader-body">
          <TableOfContents paragraphs={paragraphs}/>
          <div id="content-container">
            <div id="content">
              {translation &&
                <header className="reader-head">
                  {translation.author &&
                    <p className="reader-head__author">{translation.author}</p>}
                  <h1 className="reader-head__title">{translation.title}</h1>
                  <div className="reader-head__rule" aria-hidden="true">
                    <span/>
                  </div>
                </header>}
              <Paragraphs
                  paragraphs={paragraphs}
                  scrollPosition={position}
                  language={language}/>
            </div>
          </div>
        </div>
      </div>
  );
}
