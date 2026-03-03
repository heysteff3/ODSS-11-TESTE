import { useEffect, useState } from 'react'
import api from '../api/client'
import { AreaChart, Area, BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'

const today = new Date().toISOString().slice(0,10)
const sevenAgo = new Date(Date.now() - 7*24*60*60*1000).toISOString().slice(0,10)

const parseToken = (token) => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const username = payload.sub || payload.username || ''
    const role = payload.role || (Array.isArray(payload.roles) ? payload.roles[0] : payload.roles) || ''
    return { username, role }
  } catch {
    return { username: '', role: '' }
  }
}

export default function Dashboard() {
  const [producao, setProducao] = useState({})
  const [estoque, setEstoque] = useState({})
  const [doacoes, setDoacoes] = useState({})
  const [energia, setEnergia] = useState({})
  const [alertas, setAlertas] = useState({})
  const [usuario, setUsuario] = useState({ username: '', role: '' })

  useEffect(() => {
    const token = localStorage.getItem('token')
    if (token) setUsuario(parseToken(token))

    const load = async () => {
      const [p, e, d, en, a] = await Promise.all([
        api.get(`/dashboard/producao?inicio=${sevenAgo}&fim=${today}`),
        api.get('/dashboard/estoque'),
        api.get(`/dashboard/doacoes?inicio=${sevenAgo}&fim=${today}`),
        api.get(`/dashboard/energia?inicio=${sevenAgo}&fim=${today}`),
        api.get('/dashboard/alertas')
      ])
      setProducao(p.data)
      setEstoque(e.data)
      setDoacoes(d.data)
      setEnergia(en.data)
      setAlertas(a.data)
    }
    load().catch(console.error)
  }, [])

  const chartData = Object.entries(estoque.saldo || {}).map(([name, value]) => ({ name, value }))

  return (
    <div className="grid">
      <div className="card" style={{gridColumn:'1 / -1', display:'flex', justifyContent:'space-between', alignItems:'center', gap:'1rem'}}>
        <div>
          <h3>Sessão ativa</h3>
          <p>Nome: <strong>{usuario.username || 'Não identificado'}</strong></p>
        </div>
        <div style={{textAlign:'right'}}>
          <p style={{margin:0, color:'var(--muted)'}}>Tipo de acesso</p>
          <div className="badge" style={{background:'rgba(34,211,238,0.15)', color:'#67e8f9'}}>
            {usuario.role || '—'}
          </div>
        </div>
      </div>
      <div className="card">
        <h3>Produção (7d)</h3>
        <p>Lotes: {producao.totalLotes || 0}</p>
        <p>Por unidade: {producao.porUnidade && Object.keys(producao.porUnidade).join(', ')}</p>
      </div>
      <div className="card">
        <h3>Doações</h3>
        <p>Total entregue: {doacoes.totalEntregue || 0} kg</p>
        <p>Status: {doacoes.porStatus && Object.entries(doacoes.porStatus).map(([k,v])=>`${k}:${v}`).join(' | ')}</p>
      </div>
      <div className="card">
        <h3>Energia</h3>
        <p>Gerado: {energia.geradoKwh || 0} kWh</p>
        <p>Consumido: {energia.consumidoKwh || 0} kWh</p>
        <p>Biogás: {energia.biogasM3 || 0} m3</p>
      </div>
      <div className="card">
        <h3>Alertas</h3>
        <p>Estoque: {(alertas.estoque || []).join(', ') || 'Normal'}</p>
        <p>Pedidos: {(alertas.pedidosAtrasados || []).join(', ') || 'Nenhum atraso'}</p>
        <p>Unidades: {(alertas.unidadesParadas || []).join(', ') || 'Operando'}</p>
      </div>
      <div className="card" style={{gridColumn:'1 / -1'}}>
        <h3>Estoque por produto</h3>
        <div style={{height:'260px'}}>
          <ResponsiveContainer>
            <BarChart data={chartData}>
              <XAxis dataKey="name" stroke="#94a3b8" />
              <YAxis stroke="#94a3b8" />
              <Tooltip />
              <Bar dataKey="value" fill="#22d3ee" radius={6} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  )
}
