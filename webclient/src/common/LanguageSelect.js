import {Dropdown, Icon} from "react-bulma-components";
import {useSearchParams} from "react-router-dom";

export function LanguageSelect({
  supportedLanguages,
  activeLanguage,
  onLanguageChanged
}) {

  const [searchParams, setSearchParams] = useSearchParams();

  function setLanguage(language) {
    if (onLanguageChanged !== undefined) {
      onLanguageChanged(language);
      return;
    }

    console.log('setLanguage: ', language);
    const queryFromParam = searchParams.get('q');
    const newSearchParams = {
      lang: language
    };
    if (queryFromParam !== null) {
      newSearchParams.query = queryFromParam;
    }
    setSearchParams(newSearchParams);
  }

  return (<Dropdown
    value={activeLanguage}
    onChange={setLanguage}
    icon={<Icon><i aria-hidden="true"
                   className="fa-solid fa-angle-down"/></Icon>}>
    {
      supportedLanguages.map((language) => (
        <Dropdown.Item
          value={language}
          renderAs="a">
          {language}
        </Dropdown.Item>
      ))
    }
  </Dropdown>);
}