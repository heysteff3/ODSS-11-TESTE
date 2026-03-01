import { useEffect, useState } from 'react'
import api from '../api/client'

export default function Doacoes() {
  const [pedidos, setPedidos] = useState([])
  const [beneficiarios, setBeneficiarios] = useState([])
  const [produtos, setProdutos] = useState([])
  const [form, setForm] = useState({beneficiarioId:'', produtoId:'', quantidade:0})

  const load = async () => {
    const [ped, ben, prod] = await Promise.all([
      api.get('/pedidos-doacao'),
      api.get('/beneficiarios'),
      api.get('/produtos')
    ])
    setPedidos(ped.data.content || ped.data)
    setBeneficiarios(ben.data)
    setProdutos(prod.data.content || prod.data)
  }

  useEffect(()=>{ load().catch(console.error)},[])

  const create = async (e) => {
    e.preventDefault()
    await api.post('/pedidos-doacao', {...form, quantidade:Number(form.quantidade)})
    await load()
  }

  const reservar = async (id) => {
    await api.post(`/pedidos-doacao/${id}/reservar`)
    await load()
  }

  return (
    <div className="grid">
      <div className="card">
        <h3>Novo Pedido</h3>
        <form onSubmit={create}>
          <label>Beneficiário</label>
          <select value={form.beneficiarioId} onChange={e=>setForm({...form, beneficiarioId:e.target.value})}>
            <option value="">Selecione</option>
            {beneficiarios.map(b=> <option key={b.id} value={b.id}>{b.nome}</option>)}
          </select>
          <label>Produto</label>
          <select value={form.produtoId} onChange={e=>setForm({...form, produtoId:e.target.value})}>
            <option value="">Selecione</option>
            {produtos.map(p=> <option key={p.id} value={p.id}>{p.nome}</option>)}
          </select>
          <label>Quantidade</label>
          <input type="number" value={form.quantidade} onChange={e=>setForm({...form, quantidade:e.target.value})} />
          <button className="button">Criar</button>
        </form>
      </div>
      <div className="card" style={{gridColumn:'span 2'}}>
        <h3>Pedidos</h3>
        <table className="table">
          <thead><tr><th>ID</th><th>Beneficiário</th><th>Produto</th><th>Qtd</th><th>Status</th><th></th></tr></thead>
          <tbody>
            {pedidos.map(p=>(
              <tr key={p.id}>
                <td>{p.id}</td>
                <td>{p.beneficiario?.nome}</td>
                <td>{p.produto?.nome}</td>
                <td>{p.quantidade}</td>
                <td><span className="badge">{p.status}</span></td>
                <td><button className="button" onClick={()=>reservar(p.id)}>Reservar</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

