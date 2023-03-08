import {useNavigate, useSearchParams} from 'react-router-dom';
import {Dropdown, Icon, Navbar, Form} from 'react-bulma-components';
import {SearchBar} from '../search/SearchBar';
import {LanguageSelect} from "./LanguageSelect";

export function TopBar({supportedLanguages, activeLanguage}) {

  return (<Navbar color="primary" active={true} fixed="top">
    <Navbar.Brand>
      <Navbar.Item renderAs="div">
        <SearchBar/>
      </Navbar.Item>
      <Navbar.Burger/>
    </Navbar.Brand>
    <Navbar.Menu>
      <Navbar.Container>
      </Navbar.Container>
      <Navbar.Container align="right">
        <Navbar.Item href="/">
          <Icon><i aria-hidden="true" className="fa-solid fa-list"/></Icon>
        </Navbar.Item>
        <Navbar.Item href="/translate">
          <Icon><i aria-hidden="true" className="fa-solid fa-language"/></Icon>
        </Navbar.Item>
        <Navbar.Item renderAs="div">
          <LanguageSelect
            activeLanguage={activeLanguage}
            supportedLanguages={supportedLanguages} />
        </Navbar.Item>
      </Navbar.Container>
    </Navbar.Menu>
  </Navbar>);
}