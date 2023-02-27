import {useNavigate, useSearchParams} from 'react-router-dom';
import {Dropdown, Icon, Navbar, Form} from 'react-bulma-components';
import {SearchBar} from '../search/SearchBar';

export function TopBar({supportedLanguages, activeLanguage}) {

  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();

  function setLanguage(language) {
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
          <Dropdown
              value={activeLanguage}
              onChange={setLanguage}
              icon={<Icon><i aria-hidden="true"
                             className="fa-solid fa-angle-down"/></Icon>}>
            {
              supportedLanguages.map((language) => (
                  <Dropdown.Item value={language}>
                    {language}
                  </Dropdown.Item>
              ))
            }
          </Dropdown>
        </Navbar.Item>
      </Navbar.Container>
    </Navbar.Menu>
  </Navbar>);
}