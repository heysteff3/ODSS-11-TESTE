import { useEffect, useState, useMemo } from 'react'
import api from '../api/client'
import useDebounce from '../hooks/useDebounce'
import useOnlineStatus from '../hooks/useOnlineStatus'

export default function Doacoes() {
  const [pedidos, setPedidos] = useState([])
  const [beneficiarios, setBeneficiarios] = useState([])
  const [produtos, setProdutos] = useState([])
  const [form, setForm] = useState({beneficiarioId:'', produtoId:'', quantidade:0})
  const [filtro, setFiltro] = useState('')
  const [loading, setLoading] = useState(false)
  const debouncedFiltro = useDebounce(filtro, 250)
  const online = useOnlineStatus()

  const load = async () => {
    setLoading(true)
    try {
      const [ped, ben, prod] = await Promise.all([
        api.get('/pedidos-doacao'),
        api.get('/beneficiarios'),
        api.get('/produtos')
      ])
      const pedidosData = ped.data.content || ped.data
      const prodData = prod.data.content || prod.data
      setPedidos(pedidosData)
      setBeneficiarios(ben.data)
      setProdutos(prodData)
      sessionStorage.setItem('pedidos-cache', JSON.stringify(pedidosData))
    } catch (err) {
      const cached = sessionStorage.getItem('pedidos-cache')
      if (cached) setPedidos(JSON.parse(cached))
      console.error(err)
    } finally {
      setLoading(false)
    }
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

  const filteredPedidos = useMemo(() => {
    if (!debouncedFiltro) return pedidos
    return pedidos.filter(p => p.beneficiario?.nome?.toLowerCase().includes(debouncedFiltro.toLowerCase()) || p.produto?.nome?.toLowerCase().includes(debouncedFiltro.toLowerCase()))
  }, [pedidos, debouncedFiltro])

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
        <p className="muted" style={{marginTop:'0.5rem'}}>{online ? 'Sincronizado' : 'Offline - alterações serão enviadas ao voltar'}</p>
      </div>
      <div className="card" style={{gridColumn:'span 2'}}>
        <div style={{display:'flex', justifyContent:'space-between', alignItems:'center'}}>
          <h3>Pedidos</h3>
          <input style={{maxWidth:'260px'}} placeholder="Filtrar por beneficiário ou produto" value={filtro} onChange={(e)=>setFiltro(e.target.value)} />
        </div>
        {loading ? <div className="skeleton" style={{height:'180px'}} /> : (
          <table className="table">
            <thead><tr><th>ID</th><th>Beneficiário</th><th>Produto</th><th>Qtd</th><th>Status</th><th></th></tr></thead>
            <tbody>
              {filteredPedidos.map(p=>(
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
        )}
      </div>
    </div>
  )
}
