import { useEffect, useState } from 'react'
import api from '../api/client'
import { AreaChart, Area, BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'

const today = new Date().toISOString().slice(0,10)
const sevenAgo = new Date(Date.now() - 7*24*60*60*1000).toISOString().slice(0,10)

export default function Dashboard() {
  const [producao, setProducao] = useState({})
  const [estoque, setEstoque] = useState({})
  const [doacoes, setDoacoes] = useState({})
  const [energia, setEnergia] = useState({})
  const [alertas, setAlertas] = useState({})

  useEffect(() => {
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

