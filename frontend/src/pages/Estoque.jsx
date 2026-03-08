import { useEffect, useState, useMemo } from 'react'
import api from '../api/client'
import useDebounce from '../hooks/useDebounce'
import useOnlineStatus from '../hooks/useOnlineStatus'
import ScannerModal from '../components/ScannerModal'

export default function Estoque() {
  const [itens, setItens] = useState([])
  const [produtos, setProdutos] = useState([])
  const [mov, setMov] = useState({produtoId:'', armazemId:'', tipo:'ENTRADA', quantidade:0})
  const [filtro, setFiltro] = useState('')
  const [loading, setLoading] = useState(false)
  const [scannerAberto, setScannerAberto] = useState(false)
  const [scannerTipo, setScannerTipo] = useState('ENTRADA')
  const debouncedFiltro = useDebounce(filtro, 250)
  const online = useOnlineStatus()

  const load = async () => {
    setLoading(true)
    try {
      const [estoqueRes, prodRes] = await Promise.all([
        api.get('/estoques'), api.get('/produtos')
      ])
      const estoqueData = estoqueRes.data.content || estoqueRes.data
      const prodData = prodRes.data.content || prodRes.data
      setItens(estoqueData)
      setProdutos(prodData)
      sessionStorage.setItem('estoque-cache', JSON.stringify(estoqueData))
      sessionStorage.setItem('produtos-cache', JSON.stringify(prodData))
    } catch (err) {
      const cachedEstoque = sessionStorage.getItem('estoque-cache')
      const cachedProd = sessionStorage.getItem('produtos-cache')
      if (cachedEstoque) setItens(JSON.parse(cachedEstoque))
      if (cachedProd) setProdutos(JSON.parse(cachedProd))
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(()=>{ load().catch(console.error)},[])

  const movimentar = async (e) => {
    e?.preventDefault?.()
    await api.post('/estoques/movimentacoes', {...mov, quantidade:Number(mov.quantidade), armazemId: mov.armazemId || 1, unidadeMedida:'kg'})
    await load()
  }

  const filteredItens = useMemo(() => {
    if (!debouncedFiltro) return itens
    return itens.filter((i) => i.produto?.nome?.toLowerCase().includes(debouncedFiltro.toLowerCase()))
  }, [itens, debouncedFiltro])

  const handleScan = (codigo) => {
    setMov((m) => ({ ...m, produtoId: codigo, tipo: scannerTipo }))
  }

  return (
    <div className="grid">
      <div className="card">
        <h3>Movimentar Estoque</h3>
        <div className="pill-row" style={{marginBottom:'0.5rem'}}>
          <button className={pill } type="button" onClick={()=>setMov({...mov, tipo:'ENTRADA'})}>Entrada</button>
          <button className={pill } type="button" onClick={()=>setMov({...mov, tipo:'SAIDA'})}>Saída</button>
          <button className={pill } type="button" onClick={()=>setMov({...mov, tipo:'AJUSTE'})}>Ajuste</button>
        </div>
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
        <div className="inline-fields" style={{marginTop:'0.5rem'}}>
          <button className="button" type="button" onClick={()=>{setScannerTipo('ENTRADA'); setScannerAberto(true)}}>Check-in QR</button>
          <button className="button" type="button" onClick={()=>{setScannerTipo('SAIDA'); setScannerAberto(true)}}>Check-out QR</button>
        </div>
        <p className="muted" style={{marginTop:'0.5rem'}}>{online ? 'Tempo real ativado' : 'Offline: usando cache local'}</p>
      </div>
      <div className="card" style={{gridColumn:'span 2'}}>
        <div style={{display:'flex', justifyContent:'space-between', alignItems:'center'}}>
          <h3>Saldo</h3>
          <input style={{maxWidth:'260px'}} placeholder="Filtrar produto" value={filtro} onChange={(e)=>setFiltro(e.target.value)} />
        </div>
        {loading ? <div className="skeleton" style={{height:'180px'}} /> : (
          <table className="table">
            <thead><tr><th>Produto</th><th>Qtd</th><th>Armazém</th></tr></thead>
            <tbody>
              {filteredItens.map(i=> (
                <tr key={i.id}>
                  <td>{i.produto?.nome}</td>
                  <td>{i.quantidade}</td>
                  <td>{i.armazem?.nome}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <ScannerModal
        open={scannerAberto}
        onClose={()=>setScannerAberto(false)}
        onDetect={(code)=>handleScan(code)}
        title={Leitor ()}
      />
    </div>
  )
}
