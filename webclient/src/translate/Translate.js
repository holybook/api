import {useEffect, useState} from 'react';
import {TopBar} from '../common/TopBar';
import './Translate.scss';
import {LanguageSelect} from '../common/LanguageSelect';
import {useLoaderData} from 'react-router-dom';
import {Form} from 'react-bulma-components';

export function Translate() {
  const [fromLanguage, setFromLanguage] = useState('en');
  const [toLanguage, setToLanguage] = useState('de');
  const [translationResult, setTranslationResult] = useState(null);
  const [textToBeTranslated, setTextToBeTranslated] = useState('');
  const supportedLanguages = useLoaderData();

  async function submitTranslation() {
    const translationRequest = {
      fromLanguage: fromLanguage,
      toLanguage: toLanguage,
      text: textToBeTranslated
    };
    const response = await fetch('/api/translate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(translationRequest)
    });
    if (!response.ok) {
      setTranslationResult(null);
      return;
    }
    setTranslationResult(await response.json());
  }

  function getTranslatedText() {
    if (translationResult !== null) {
      return translationResult.translatedParagraph.text;
    }

    if (textToBeTranslated === '') {
      return '';
    }

    return null;
  }

  useEffect(() => {
    if (textToBeTranslated.length > 0) {
      submitTranslation();
    }
  }, [fromLanguage, toLanguage, textToBeTranslated])

  return (<div id="translate">
      <TopBar
        activeLanguage={fromLanguage}
        supportedLanguages={supportedLanguages}/>
      <div className="language-header">
        <div className="language-container left">
          <LanguageSelect
            supportedLanguages={supportedLanguages}
            activeLanguage={fromLanguage}
            onLanguageChanged={setFromLanguage}
          />
        </div>
        <div className="language-container right">
          <LanguageSelect
            supportedLanguages={supportedLanguages}
            activeLanguage={toLanguage}
            onLanguageChanged={setToLanguage}
          />
        </div>
      </div>
      <div className="text-container">
        <div className="translate-text">
          <Form.Textarea
            fixedSize={true}
            onChange={(event) => setTextToBeTranslated(event.target.value)}
          />
        </div>
        <ResultText
          translatedText={getTranslatedText()}
          language={toLanguage}/>
      </div>
      <div className="attribution-container">
        <Attribution
          translationResult={translationResult}
          toLanguage={toLanguage}
        />
      </div>
    </div>
  )
}

function Attribution({translationResult, toLanguage}) {
  if (translationResult === null) {
    return <div/>;
  }

  const originalResult = translationResult.allOriginalResults[0];

  return (<a
    href={`/books/${originalResult.bookId}?lang=${toLanguage}&pos=${translationResult.translatedParagraph.index}:60`}
    className="translation-attribution">
    &mdash; {originalResult.author}, {originalResult.title}, par. {translationResult.translatedParagraph.number}
  </a>);
}

function ResultText({translatedText, language}) {
  if (translatedText === null) {
    return (
      <div className="translate-text error">
        Could not find paragraph in language {language}.
      </div>
    )
  }

  return (
    <div className="translate-text">
      <Form.Textarea
        fixedSize={true}
        value={translatedText}
      /></div>);
}