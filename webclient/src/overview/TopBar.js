import {useNavigate} from 'react-router-dom';
import {Dropdown, Icon, Navbar} from 'react-bulma-components';

export function TopBar({supportedLanguages, activeLanguage}) {

  const navigate = useNavigate();

  function setLanguage(language) {
    navigate(`/?lang=${language}`);
  }

  return (<Navbar color="primary" active={true} fixed="top">
    <Navbar.Brand>
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
        <Navbar.Item>
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