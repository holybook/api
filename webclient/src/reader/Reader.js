import {useLoaderData, useSearchParams} from "react-router-dom";
import {Paragraphs} from "./Paragraphs";

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

  return (
      <div className="reader">
        <TopBar
            book={book}
            activeLanguage={language}
            supportedLanguages={supportedLanguages}/>
        <div id="content-container">
          <div id="content">
            <Paragraphs
                paragraphs={paragraphs}
                scrollPosition={position}/>
          </div>
        </div>
      </div>
  );
}