import { useEffect, useState } from 'react'
import api from '../api/client'
import { AreaChart, Area, BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'
import useRealtime from '../hooks/useRealtime'
import StatusStrip from '../components/StatusStrip'

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

const tarefasPorRole = {
  ADMIN: [
    { label: 'Usuários', to: '/admin/usuarios' },
    { label: 'Unidades', to: '/producao' },
    { label: 'Relatórios', to: '/' }
  ],
  OPERADOR: [
    { label: 'Check-in estoque', to: '/estoque' },
    { label: 'Produção', to: '/producao' },
    { label: 'Doações', to: '/doacoes' }
  ],
  LOGISTICA: [
    { label: 'Rotas', to: '/doacoes' },
    { label: 'Saídas', to: '/estoque' },
    { label: 'Mapa ao vivo', to: '/' }
  ],
  ONG: [
    { label: 'Pedidos', to: '/doacoes' },
    { label: 'Retiradas', to: '/estoque' },
    { label: 'Status pedido', to: '/doacoes' }
  ]
}

const mockFilas = () => ({
  aguardando: Math.floor(Math.random()*8)+2,
  emSeparacao: Math.floor(Math.random()*5)+1,
  emRota: Math.floor(Math.random()*6)+1,
  tempoMedioMin: 18 + Math.floor(Math.random()*12)
})

const mockRotas = () => ([
  { rota: 'Zona Norte', veiculo: 'Van 3', entregas: 12, progresso: 70 },
  { rota: 'Centro', veiculo: 'Bike 2', entregas: 6, progresso: 45 },
  { rota: 'Zona Oeste', veiculo: 'Caminhão 1', entregas: 18, progresso: 55 }
])

const mockCritico = () => ({
  itens: [
    { nome: 'Arroz 5kg', saldo: 22, unidade: 'kg' },
    { nome: 'Feijão 1kg', saldo: 14, unidade: 'kg' },
    { nome: 'Proteína vegetal', saldo: 8, unidade: 'kg' }
  ]
})

const mockEnergia = () => ({
  gerado: 120 + Math.floor(Math.random()*40),
  consumido: 110 + Math.floor(Math.random()*25),
  biogas: 32 + Math.floor(Math.random()*4)
})

export default function Dashboard() {
  const [producao, setProducao] = useState({})
  const [estoque, setEstoque] = useState({})
  const [doacoes, setDoacoes] = useState({})
  const [energia, setEnergia] = useState({})
  const [alertas, setAlertas] = useState({})
  const [usuario, setUsuario] = useState({ username: '', role: '' })
  const { data: filas, loading: loadFilas, online, latencyMs } = useRealtime('/realtime/filas', { interval: 8000, mockFactory: mockFilas })
  const { data: rotas, loading: loadRotas } = useRealtime('/realtime/rotas', { interval: 10000, mockFactory: mockRotas })
  const { data: critico, loading: loadCritico } = useRealtime('/dashboard/estoque-critico', { interval: 12000, mockFactory: mockCritico })
  const { data: energiaLive, loading: loadEnergiaLive } = useRealtime('/dashboard/energia/live', { interval: 8000, mockFactory: mockEnergia })

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
  const roleKey = (usuario.role || '').toUpperCase()
  const tarefas = tarefasPorRole[roleKey] || tarefasPorRole.OPERADOR

  return (
    <div className="grid">
      <div className="card" style={{gridColumn:'1 / -1', display:'flex', justifyContent:'space-between', alignItems:'center', gap:'1rem'}}>
        <div>
          <h3>Sessão ativa</h3>
          <p>Nome: <strong>{usuario.username || 'Não identificado'}</strong></p>
          <div className="pill-row">
            {tarefas.map(t => <a key={t.label} href={t.to} className="pill">{t.label}</a>)}
          </div>
        </div>
        <div style={{textAlign:'right'}}>
          <p style={{margin:0, color:'var(--muted)'}}>Tipo de acesso</p>
          <div className="badge" style={{background:'rgba(34,211,238,0.15)', color:'#67e8f9'}}>
            {usuario.role || '—'}
          </div>
        </div>
      </div>

      <div className="card" style={{gridColumn:'1 / -1'}}>
        <StatusStrip online={online} latencyMs={latencyMs} />
      </div>

      <div className="card">
        <h3>Produção (7d)</h3>
        <p>Lotes: {producao.totalLotes || 0}</p>
        <p>Por unidade: {producao.porUnidade && Object.keys(producao.porUnidade).join(', ')}</p>
      </div>
      <div className="card">
        <h3>Doações</h3>
        <p>Total entregue: {doacoes.totalEntregue || 0} kg</p>
        <p>Status: {doacoes.porStatus && Object.entries(doacoes.porStatus).map(([k,v])=>`${k}: ${v}`).join(' | ')}</p>
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
        <p>Validade: {(alertas.validade || []).join(', ') || 'Sem vencimentos'}</p>
        <p>Pedidos: {(alertas.pedidosAtrasados || []).join(', ') || 'Nenhum atraso'}</p>
        <p>Unidades: {(alertas.unidadesParadas || []).join(', ') || 'Operando'}</p>
      </div>

      <div className="card">
        <h3>Filas em tempo real</h3>
        {loadFilas ? <div className="skeleton" style={{height:'90px'}} /> : (
          <div className="stats">
            <div>
              <p className="muted">Aguardando</p>
              <h2>{filas?.aguardando ?? '—'}</h2>
            </div>
            <div>
              <p className="muted">Separação</p>
              <h2>{filas?.emSeparacao ?? '—'}</h2>
            </div>
            <div>
              <p className="muted">Em rota</p>
              <h2>{filas?.emRota ?? '—'}</h2>
            </div>
            <div>
              <p className="muted">TMA</p>
              <h2>{filas?.tempoMedioMin ? `${filas.tempoMedioMin} min` : '—'}</h2>
            </div>
          </div>
        )}
      </div>

      <div className="card map-card">
        <h3>Mapa de rotas</h3>
        {loadRotas ? <div className="skeleton" style={{height:'160px'}} /> : (
          <div className="route-list">
            {(rotas || []).map((r) => (
              <div key={r.rota} className="route-row">
                <div>
                  <strong>{r.rota}</strong>
                  <p className="muted">{r.veiculo} · {r.entregas} entregas</p>
                </div>
                <div className="progress">
                  <div className="bar" style={{width:`${r.progresso}%`}}></div>
                </div>
                <span className="muted">{r.progresso}%</span>
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="card">
        <h3>Estoque crítico</h3>
        {loadCritico ? <div className="skeleton" style={{height:'100px'}} /> : (
          <ul className="list">
            {(critico?.itens || []).map((i) => (
              <li key={i.nome}>
                <div>
                  <strong>{i.nome}</strong>
                  <p className="muted">Saldo: {i.saldo} {i.unidade}</p>
                </div>
                <span className="badge" style={{background:'rgba(248,113,113,0.2)', color:'#fca5a5'}}>Baixo</span>
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="card">
        <h3>Energia ao vivo</h3>
        {loadEnergiaLive ? <div className="skeleton" style={{height:'90px'}} /> : (
          <div className="stats">
            <div>
              <p className="muted">Gerado</p>
              <h2>{energiaLive?.gerado ?? '—'} kWh</h2>
            </div>
            <div>
              <p className="muted">Consumido</p>
              <h2>{energiaLive?.consumido ?? '—'} kWh</h2>
            </div>
            <div>
              <p className="muted">Biogás</p>
              <h2>{energiaLive?.biogas ?? '—'} m³</h2>
            </div>
          </div>
        )}
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
