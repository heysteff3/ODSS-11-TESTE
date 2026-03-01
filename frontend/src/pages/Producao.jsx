import { useEffect, useState } from 'react'
import api from '../api/client'

export default function Producao() {
  const [lotes, setLotes] = useState([])
  const [produtos, setProdutos] = useState([])
  const [unidades, setUnidades] = useState([])
  const [form, setForm] = useState({produtoId:'', unidadeId:'', quantidade:0, unidadeMedida:'kg'})

  const load = async () => {
    const [lotesRes, prodRes, uniRes] = await Promise.all([
      api.get('/lotes'), api.get('/produtos'), api.get('/unidades-produtivas')
    ])
    setLotes(lotesRes.data.content || lotesRes.data)
    setProdutos(prodRes.data.content || prodRes.data)
    setUnidades(uniRes.data)
  }

  useEffect(() => { load().catch(console.error) }, [])

  const submit = async (e) => {
    e.preventDefault()
    await api.post('/lotes', {...form, quantidade:Number(form.quantidade)})
    await load()
  }

  return (
    <div className="grid">
      <div className="card">
        <h3>Novo Lote</h3>
        <form onSubmit={submit}>
          <label>Produto</label>
          <select value={form.produtoId} onChange={e=>setForm({...form, produtoId:e.target.value})}>
            <option value="">Selecione</option>
            {produtos.map(p=> <option key={p.id} value={p.id}>{p.nome}</option>)}
          </select>
          <label>Unidade</label>
          <select value={form.unidadeId} onChange={e=>setForm({...form, unidadeId:e.target.value})}>
            <option value="">Selecione</option>
            {unidades.map(u=> <option key={u.id} value={u.id}>{u.nome}</option>)}
          </select>
          <div className="inline-fields">
            <div>
              <label>Quantidade</label>
              <input type="number" value={form.quantidade} onChange={e=>setForm({...form, quantidade:e.target.value})} />
            </div>
            <div>
              <label>Unidade Medida</label>
              <input value={form.unidadeMedida} onChange={e=>setForm({...form, unidadeMedida:e.target.value})} />
            </div>
          </div>
          <button className="button" type="submit">Salvar</button>
        </form>
      </div>
      <div className="card" style={{gridColumn:'span 2'}}>
        <h3>Lotes</h3>
        <table className="table">
          <thead>
            <tr><th>ID</th><th>Produto</th><th>Unidade</th><th>Qtd</th><th>Status</th></tr>
          </thead>
          <tbody>
            {lotes.map(l=> (
              <tr key={l.id}>
                <td>{l.id}</td>
                <td>{l.produto?.nome}</td>
                <td>{l.unidade?.nome}</td>
                <td>{l.quantidade}</td>
                <td><span className="badge">{l.status}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

