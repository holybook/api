import {useState} from 'react';
import {TopBar} from '../common/TopBar';
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

  return (<div id="aitranslate">
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
            value={textToBeTranslated}
            onChange={(event) => setTextToBeTranslated(event.target.value)}
          />
          <div className="translate-button-container">
            <Button 
              color="primary"
              onClick={submitTranslation}
              loading={isLoading}
              disabled={!textToBeTranslated.trim() || isLoading}
            >
              Translate with AI
            </Button>
          </div>
        </div>
        <div className="result-container">
          {translationResponse && 
            <TranslatedParagraphs paragraphs={translationResponse.paragraphs} toLanguage={toLanguage} />}
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
            href={`/books/${paragraph.annotation.bookId}?lang=${toLanguage}&pos=${paragraph.annotation.index}:60`}
          >
            &mdash; {paragraph.annotation.title}, par. {paragraph.annotation.number}
          </a>
        </div>
      )}
    </div>
  );
}
