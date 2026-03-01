import { Routes, Route, Link, useNavigate } from 'react-router-dom'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Producao from './pages/Producao'
import Estoque from './pages/Estoque'
import Doacoes from './pages/Doacoes'
import Energia from './pages/Energia'

function Header() {
  const navigate = useNavigate()
  const logout = () => {
    localStorage.removeItem('token')
    navigate('/login')
  }
  return (
    <header>
      <div style={{display:'flex',alignItems:'center',gap:'10px'}}>
        <Link to="/">
          <img src="/logo.png" alt="SustentaFome" className="logo" />
        </Link>
      </div>
      <nav>
        <Link to="/">Dashboard</Link>
        <Link to="/producao">Produção</Link>
        <Link to="/estoque">Estoque</Link>
        <Link to="/doacoes">Doações</Link>
        <Link to="/energia">Energia</Link>
        <button className="button" style={{marginLeft:'1rem'}} onClick={logout}>Sair</button>
      </nav>
    </header>
  )
}

export default function App() {
  return (
    <div>
      <Header />
      <div className="container">
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/producao" element={<Producao />} />
          <Route path="/estoque" element={<Estoque />} />
          <Route path="/doacoes" element={<Doacoes />} />
          <Route path="/energia" element={<Energia />} />
          <Route path="/" element={<Dashboard />} />
        </Routes>
      </div>
    </div>
  )
}
