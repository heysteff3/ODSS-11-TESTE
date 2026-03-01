import { useEffect, useState } from 'react'
import api from '../api/client'

const today = new Date().toISOString().slice(0,10)
const weekAgo = new Date(Date.now() - 7*24*60*60*1000).toISOString().slice(0,10)

export default function Energia() {
  const [sim, setSim] = useState({})
  const [periodo, setPeriodo] = useState({inicio: weekAgo, fim: today})

  const load = async () => {
    const res = await api.get(`/energia/simulacao?inicio=${periodo.inicio}&fim=${periodo.fim}`)
    setSim(res.data)
  }

  useEffect(()=>{ load().catch(console.error)},[])

  return (
    <div className="grid">
      <div className="card">
        <h3>Período</h3>
        <input type="date" value={periodo.inicio} onChange={e=>setPeriodo({...periodo, inicio:e.target.value})} />
        <input type="date" value={periodo.fim} onChange={e=>setPeriodo({...periodo, fim:e.target.value})} />
        <button className="button" onClick={load}>Simular</button>
      </div>
      <div className="card">
        <h3>Energia</h3>
        <p>Gerada: {sim.energiaGeradaKwh || 0} kWh</p>
        <p>Consumida: {sim.energiaConsumidaKwh || 0} kWh</p>
        <p>Saldo: {sim.saldoEnergeticoKwh || 0} kWh</p>
      </div>
      <div className="card">
        <h3>Indicadores</h3>
        <p>kg Alimento/kWh: {sim.kgAlimentoPorKwh?.toFixed?.(2) || 0}</p>
        <p>CO2 Recirculado: {sim.co2RecirculadoKg || 0} kg</p>
        <p>Biomassa: {sim.biomassaProcessadaKg || 0} kg</p>
        <p>Biogás: {sim.biogasProduzidoM3 || 0} m3</p>
      </div>
    </div>
  )
}

