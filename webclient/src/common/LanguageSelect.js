import {MenuItem, Select} from '@mui/material';

export function LanguageSelect({
  supportedLanguages,
  activeLanguage,
  onLanguageChanged
}) {

  function setLanguage(event) {
    const selectedLanguage = event.target.value
    onLanguageChanged(selectedLanguage)
  }

  return (<Select
      value={activeLanguage}
      onChange={setLanguage}
      size="small"
  >
    {supportedLanguages.map((language) => {
      return (<MenuItem value={language} key={language}>
        {language}
      </MenuItem>);
    })}
  </Select>);
}