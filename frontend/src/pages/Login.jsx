import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/client'

export default function Login() {
  const [username, setUsername] = useState('admin')
  const [password, setPassword] = useState('admin123')
  const [error, setError] = useState('')
  const navigate = useNavigate()

  const submit = async (e) => {
    e.preventDefault()
    try {
      const res = await api.post('/auth/login', { username, password })
      localStorage.setItem('token', res.data.token)
      navigate('/')
    } catch (err) {
      setError('Login inválido')
    }
  }

  return (
    <div className="container" style={{maxWidth:'420px'}}>
      <div className="card">
        <h2>Entrar</h2>
        <form onSubmit={submit}>
          <label>Usuário</label>
          <input value={username} onChange={e=>setUsername(e.target.value)} />
          <label>Senha</label>
          <input type="password" value={password} onChange={e=>setPassword(e.target.value)} />
          <button className="button" type="submit">Acessar</button>
        </form>
        {error && <p style={{color:'#f87171'}}>{error}</p>}
      </div>
    </div>
  )
}

