import { Routes, Route, Link, useNavigate, Navigate, useLocation } from 'react-router-dom'
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

function RequireAuth({ children }) {
  const token = localStorage.getItem('token')
  if (!token) return <Navigate to="/login" replace />
  return children
}

export default function App() {
  const location = useLocation()
  const token = localStorage.getItem('token')
  const showHeader = location.pathname !== '/login'

  return (
    <div>
      {showHeader && <Header />}
      <div className="container">
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/producao" element={<RequireAuth><Producao /></RequireAuth>} />
          <Route path="/estoque" element={<RequireAuth><Estoque /></RequireAuth>} />
          <Route path="/doacoes" element={<RequireAuth><Doacoes /></RequireAuth>} />
          <Route path="/energia" element={<RequireAuth><Energia /></RequireAuth>} />
          <Route path="/" element={token ? <Dashboard /> : <Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to={token ? '/' : '/login'} replace />} />
        </Routes>
      </div>
    </div>
  )
}
