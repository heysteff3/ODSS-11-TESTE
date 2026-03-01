import { useEffect, useState } from 'react'
import api from '../api/client'

export default function Estoque() {
  const [itens, setItens] = useState([])
  const [produtos, setProdutos] = useState([])
  const [mov, setMov] = useState({produtoId:'', armazemId:'', tipo:'ENTRADA', quantidade:0})

  const load = async () => {
    const [estoqueRes, prodRes] = await Promise.all([
      api.get('/estoques'), api.get('/produtos')
    ])
    setItens(estoqueRes.data.content || estoqueRes.data)
    setProdutos(prodRes.data.content || prodRes.data)
  }

  useEffect(()=>{ load().catch(console.error)},[])

  const movimentar = async (e) => {
    e.preventDefault()
    await api.post('/estoques/movimentacoes', {...mov, quantidade:Number(mov.quantidade), armazemId: mov.armazemId || 1, unidadeMedida:'kg'})
    await load()
  }

  return (
    <div className="grid">
      <div className="card">
        <h3>Movimentar Estoque</h3>
        <form onSubmit={movimentar}>
          <label>Produto</label>
          <select value={mov.produtoId} onChange={e=>setMov({...mov, produtoId:e.target.value})}>
            <option value="">Selecione</option>
            {produtos.map(p=> <option key={p.id} value={p.id}>{p.nome}</option>)}
          </select>
          <label>Tipo</label>
          <select value={mov.tipo} onChange={e=>setMov({...mov, tipo:e.target.value})}>
            <option>ENTRADA</option><option>SAIDA</option><option>AJUSTE</option>
          </select>
          <label>Quantidade</label>
          <input type="number" value={mov.quantidade} onChange={e=>setMov({...mov, quantidade:e.target.value})} />
          <button className="button">Registrar</button>
        </form>
      </div>
      <div className="card" style={{gridColumn:'span 2'}}>
        <h3>Saldo</h3>
        <table className="table">
          <thead><tr><th>Produto</th><th>Qtd</th><th>Armazém</th></tr></thead>
          <tbody>
            {itens.map(i=> (
              <tr key={i.id}>
                <td>{i.produto?.nome}</td>
                <td>{i.quantidade}</td>
                <td>{i.armazem?.nome}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

