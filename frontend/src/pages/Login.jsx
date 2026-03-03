import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/client'

const passwordRule = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,16}$/

export default function Login() {
  const [mode, setMode] = useState('login')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [info, setInfo] = useState('')
  const [sending, setSending] = useState(false)
  const [codeSent, setCodeSent] = useState(false)
  const [reg, setReg] = useState({
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    verificationCode: ''
  })
  const navigate = useNavigate()

  const handleLogin = async (e) => {
    e.preventDefault()
    setError('')
    try {
      const res = await api.post('/auth/login', { username, password })
      localStorage.setItem('token', res.data.token)
      navigate('/')
    } catch (err) {
      setError('Login inválido')
    }
  }

  const sendCode = async () => {
    setError('')
    setInfo('')
    if (!reg.email) {
      setError('Informe um e-mail para envio do código')
      return
    }
    setSending(true)
    try {
      const res = await api.post('/auth/email-token', { email: reg.email })
      setCodeSent(true)
      setInfo('Código enviado para o e-mail informado')
      if (res.data?.devToken) {
        setReg(r => ({ ...r, verificationCode: res.data.devToken }))
      }
    } catch (err) {
      if (err.response?.status === 409) {
        setError('E-mail já cadastrado')
      } else {
        setError('Falha ao enviar código')
      }
    } finally {
      setSending(false)
    }
  }

  const handleRegister = async (e) => {
    e.preventDefault()
    setError('')
    setInfo('')
    if (reg.password !== reg.confirmPassword) {
      setError('As senhas não coincidem')
      return
    }
    if (!passwordRule.test(reg.password)) {
      setError('Senha não atende os requisitos')
      return
    }
    try {
      const res = await api.post('/auth/register', reg)
      localStorage.setItem('token', res.data.token)
      navigate('/')
    } catch (err) {
      if (err.response?.status === 409) {
        setError('Usuário ou e-mail já cadastrado')
      } else if (err.response?.status === 400) {
        setError('Código de verificação inválido ou dados incorretos')
      } else {
        setError('Falha no cadastro')
      }
    }
  }

  const renderLogin = () => (
    <form onSubmit={handleLogin}>
      <label>Usuário</label>
      <input value={username} onChange={e=>setUsername(e.target.value)} required />
      <label>Senha</label>
      <input type="password" value={password} onChange={e=>setPassword(e.target.value)} required />
      <button className="button" type="submit">Acessar</button>
    </form>
  )

  const renderRegister = () => (
    <form onSubmit={handleRegister}>
      <div className="inline-fields">
        <div>
          <label>Nome</label>
          <input value={reg.firstName} onChange={e=>setReg(r=>({...r, firstName:e.target.value}))} required />
        </div>
        <div>
          <label>Sobrenome</label>
          <input value={reg.lastName} onChange={e=>setReg(r=>({...r, lastName:e.target.value}))} required />
        </div>
      </div>
      <label>Nome de usuário</label>
      <input value={reg.username} onChange={e=>setReg(r=>({...r, username:e.target.value}))} required />

      <label>E-mail</label>
      <div style={{display:'grid', gridTemplateColumns:'1fr auto', gap:'0.5rem', alignItems:'center'}}>
        <input type="email" value={reg.email} onChange={e=>setReg(r=>({...r, email:e.target.value}))} required />
        <button type="button" className="button" onClick={sendCode} disabled={sending}>Enviar código</button>
      </div>
      <label>Código de verificação</label>
      <input value={reg.verificationCode} onChange={e=>setReg(r=>({...r, verificationCode:e.target.value}))} placeholder={codeSent ? 'Digite o código enviado' : ''} required />

      <label>Telefone</label>
      <input value={reg.phone} onChange={e=>setReg(r=>({...r, phone:e.target.value}))} />

      <div className="inline-fields">
        <div>
          <label>Senha</label>
          <input type="password" value={reg.password} onChange={e=>setReg(r=>({...r, password:e.target.value}))} required />
        </div>
        <div>
          <label>Confirme a senha</label>
          <input type="password" value={reg.confirmPassword} onChange={e=>setReg(r=>({...r, confirmPassword:e.target.value}))} required />
        </div>
      </div>
      <div style={{fontSize:'0.85rem', color:'var(--muted)', margin:'0.25rem 0 0.75rem'}}>
        Regras: 8-16 caracteres, 1 minúscula, 1 maiúscula, 1 número e 1 especial.
      </div>
      <button className="button" type="submit">Criar conta</button>
    </form>
  )

  return (
    <div className="login-page">
      <div className="card login-card">
        <div style={{display:'flex', gap:'0.5rem', marginBottom:'1rem'}}>
          <button className="button" style={{flex:1, opacity: mode==='login'?1:0.6}} type="button" onClick={()=>{setMode('login'); setError(''); setInfo('')}}>
            Entrar
          </button>
          <button className="button" style={{flex:1, opacity: mode==='register'?1:0.6}} type="button" onClick={()=>{setMode('register'); setError(''); setInfo('')}}>
            Cadastrar
          </button>
        </div>
        {mode === 'login' ? renderLogin() : renderRegister()}
        {info && <p style={{color:'#67e8f9'}}>{info}</p>}
        {error && <p style={{color:'#f87171'}}>{error}</p>}
      </div>
    </div>
  )
}