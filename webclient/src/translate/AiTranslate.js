import {useState} from 'react';
import {TopBar} from '../common/TopBar';
import './Translate.scss';
import './AiTranslate.scss';
import {LanguageSelect} from '../common/LanguageSelect';
import {useLoaderData} from 'react-router-dom';
import {Form, Button} from 'react-bulma-components';

export function AiTranslate() {
  const [fromLanguage, setFromLanguage] = useState('en');
  const [toLanguage, setToLanguage] = useState('de');
  const [translationResponse, setTranslationResponse] = useState(null);
  const [textToBeTranslated, setTextToBeTranslated] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const supportedLanguages = useLoaderData();

  async function submitTranslation() {
    if (!textToBeTranslated.trim()) return;

    setIsLoading(true);
    try {
      const translationRequest = {
        fromLanguage: fromLanguage,
        toLanguage: toLanguage,
        text: textToBeTranslated
      };
      const response = await fetch('/api/aitranslate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(translationRequest)
      });
      if (!response.ok) {
        setTranslationResponse(null);
        return;
      }
      setTranslationResponse(await response.json());
    } catch (error) {
      console.error('Translation error:', error);
      setTranslationResponse(null);
    } finally {
      setIsLoading(false);
    }
  }

  return (<div id="aitranslate" className="translate-page">
      <TopBar
        activeLanguage={fromLanguage}
        supportedLanguages={supportedLanguages}/>
      <div className="translate-shell">
        <header className="translate-intro">
          <h1 className="translate-intro__title">
            AI translation
            <span className="ai-badge">
              <i className="fa-solid fa-wand-magic-sparkles" aria-hidden="true"/>
              Beta
            </span>
          </h1>
          <p className="translate-intro__subtitle">
            Render a passage into another language in the style of the
            authorized translations.
          </p>
        </header>

        <div className="language-header">
          <div className="language-container left">
            <LanguageSelect
              supportedLanguages={supportedLanguages}
              activeLanguage={fromLanguage}
              onLanguageChanged={setFromLanguage}
            />
          </div>
          <div className="language-swap" aria-hidden="true">
            <i className="fa-solid fa-arrow-right-long"/>
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
          <div className="translate-panel">
            <span className="translate-panel__label">Original</span>
            <div className="translate-text">
              <Form.Textarea
                fixedSize={true}
                value={textToBeTranslated}
                placeholder="Enter the text you want translated…"
                onChange={(event) => setTextToBeTranslated(event.target.value)}
              />
            </div>
            <div className="translate-button-container">
              <Button
                color="primary"
                onClick={submitTranslation}
                loading={isLoading}
                disabled={!textToBeTranslated.trim() || isLoading}>
                Translate with AI
              </Button>
            </div>
          </div>
          <div className="translate-panel">
            <span className="translate-panel__label">Translation</span>
            <div className="result-container">
              {translationResponse
                ? <TranslatedParagraphs
                    paragraphs={translationResponse.paragraphs}
                    toLanguage={toLanguage}/>
                : <div className="result-placeholder">
                    <i className="fa-solid fa-feather-pointed" aria-hidden="true"/>
                    <span>Your translation will appear here.</span>
                  </div>}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function TranslatedParagraphs({paragraphs, toLanguage}) {
  if (!paragraphs || paragraphs.length === 0) {
    return <div className="no-results">No translation results available</div>;
  }

  return (
    <div className="translated-paragraphs">
      {paragraphs.map((paragraph, index) => (
        <TranslatedParagraph key={index} paragraph={paragraph} toLanguage={toLanguage} />
      ))}
    </div>
  );
}

function TranslatedParagraph({paragraph, toLanguage}) {
  return (
    <div className="paragraph-with-annotation">
      <p className="paragraph-text">{paragraph.text}</p>
      {paragraph.annotation && (
        <div className="paragraph-annotation">
          <a
            href={`/books/${paragraph.annotation.bookId}?lang=${toLanguage}&pos=${paragraph.annotation.index}:60`}>
            <i className="fa-solid fa-bookmark" aria-hidden="true"/>
            {paragraph.annotation.title}, par. {paragraph.annotation.number}
          </a>
        </div>
      )}
    </div>
  );
}
