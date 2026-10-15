import { useState } from 'react';

const API = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const initial = {name:'',email:'',password:'',role:'USER',zipCode:'',street:'',addressNumber:'',complement:'',city:'',state:''};

export default function App() {
  const [form,setForm] = useState(initial);
  const [busy,setBusy] = useState(false);
  const [message,setMessage] = useState('');
  const [status,setStatus] = useState('idle');
  const change = event => setForm(current => ({...current,[event.target.name]:event.target.value}));
  async function lookupZip() {
    const cep = form.zipCode.replace(/\D/g,'');
    if(cep.length!==8) return;
    try {
      const res = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
      if(!res.ok) throw new Error();
      const address = await res.json();
      if(address.erro) { setMessage('CEP não encontrado.'); return; }
      setForm(current=>({...current,street:address.logradouro||current.street,
        city:address.localidade||current.city,state:address.uf||current.state}));
    }catch{setMessage('Não foi possível consultar o CEP. Preencha o endereço manualmente.');}
  }
  async function submit(event) {
    event.preventDefault();
    setBusy(true);setMessage('Criando sua conta...');setStatus('idle');
    try{
      const token = localStorage.getItem('token');
      const response = await fetch(`${API}/auth/register`,{
        method:'POST',headers:{'Content-Type':'application/json',...(token?{Authorization:`Bearer ${token}`}:{})},
        body:JSON.stringify({...form,zipCode:form.zipCode.replace(/\D/g,''),state:form.state.toUpperCase()})
      });
      if(!response.ok) throw new Error(await response.text() || 'Falha ao cadastrar.');
      setStatus('success');setMessage('Conta criada com sucesso! Agora você pode entrar.');
      setForm(initial);
    }catch(error){setStatus('error');setMessage(error instanceof TypeError?'Servidor indisponível.':error.message);}
    finally{setBusy(false);}
  }
  const field = (name,label,type='text',extra={}) => <label key={name}>{label}
    <input name={name} value={form[name]} onChange={change} type={type} required={!['complement'].includes(name)}
      {...extra}/></label>;
  return <main className="card">
    <div className="brand">PertoDeMim</div>
    <h1>Criar conta</h1>
    <p>Encontre profissionais perto de você com um endereço de referência.</p>
    <form onSubmit={submit}>
      {field('name','Nome completo')}
      {field('email','E-mail','email',{autoComplete:'email'})}
      {field('password','Senha','password',{autoComplete:'new-password',minLength:8})}
      <label>Tipo de conta
        <select name="role" value={form.role} onChange={change}>
          <option value="USER">Quero contratar</option><option value="PROFESSIONAL">Quero oferecer serviços</option>
        </select>
      </label>
      <h2>Endereço de referência</h2>
      {field('zipCode','CEP','text',{onBlur:lookupZip,placeholder:'00000-000',maxLength:9,inputMode:'numeric'})}
      {field('street','Rua')}
      <div className="two">{field('addressNumber','Número')}{field('complement','Complemento (opcional)')}</div>
      <div className="two">{field('city','Cidade')}{field('state','UF','text',{maxLength:2})}</div>
      <button type="submit" disabled={busy}>{busy?'Cadastrando...':'Criar conta'}</button>
      <div className={status} role="status" aria-live="polite">{message}</div>
    </form>
    <footer>Já tem conta? <a href="../frontend/login.html">Entrar</a></footer>
  </main>;
}
