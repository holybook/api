import {useNavigate} from 'react-router-dom';
import HomeIcon from '@mui/icons-material/Home';

export function TopBar({activeLanguage}) {
  const navigate = useNavigate();

  function goHome() {
    navigate(`/?lang=${activeLanguage}`)
  }

  return (<div className="topbar">
    <div id="home-button" onClick={goHome}>
      <HomeIcon/>
    </div>
  </div>);
}