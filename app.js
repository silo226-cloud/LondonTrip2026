
const DAYS=[['23','שישי','גריניץ׳ והתמזה'],['24','שבת','המצודה ומדאם טוסו'],['25','ראשון','מוזיאונים ושווקים'],['26','שני','הארמון ווסטמינסטר'],['27','שלישי','Sky Garden והמוזיאונים'],['28','רביעי','טוטנהאם'],['29','חמישי','חזרה הביתה']];
const P={greenwich:[51.4826,-0.0077],queens:[51.4811,-0.0033],meridian:[51.4779,0.0015],tunnel:[51.4865,-0.0086],pier:[51.4837,-0.0096],tower:[51.5081,-0.0759],bridge:[51.5055,-0.0754],borough:[51.5055,-0.0910],londonbridge:[51.5079,-0.0877],tussauds:[51.523,-0.1543],british:[51.5194,-0.1269],camden:[51.5416,-0.1464],covent:[51.5117,-0.123],wellington:[51.5026,-0.1505],palace:[51.5014,-0.1419],abbey:[51.4993,-0.1273],bigben:[51.5007,-0.1246],frameless:[51.5132,-0.159],natural:[51.4967,-0.1764],science:[51.4978,-0.1745],paul:[51.5138,-0.0984],millennium:[51.5095,-0.0986],leadenhall:[51.5128,-0.0836],sky:[51.5113,-0.0836],horizon:[51.5154,-0.0826],tottenham:[51.6043,-0.0663]};
const photos={greenwich:'photo-1533929736458-ca588d08c8be',tower:'photo-1513635269975-59663e0ac1ad',market:'photo-1555529669-e69e7aa0ba9a',museum:'photo-1566127992631-137a642a90f4',garden:'photo-1486406146926-c627a92ad1ab',football:'photo-1522778119026-d647f0596c20',city:'photo-1505761671935-60b3a7427bad'};
const pic=k=>'https://images.unsplash.com/'+(photos[k]||photos.city)+'?w=640&q=75&fit=crop';
const row=(day,time,end,title,key,category='city',status='planned',note='')=>({id:crypto.randomUUID(),day,time,end,title,place:title,coords:P[key]||null,photo:pic(category),status,note});
const SEED=[
row('23','11:00','12:00','Queen’s House', 'queens','greenwich','booked','הזמנה קיימת ביומן בשעה 11:00'),
row('23','12:30','13:25','Greenwich Market','greenwich','market'),
row('23','14:00','15:00','קו האורך ותצפית גריניץ׳','meridian','greenwich'),
row('23','15:10','15:40','Greenwich Foot Tunnel','tunnel','greenwich'),
row('23','16:30','17:30','שייט על התמזה – Greenwich Pier','pier','greenwich','planned','שעת שייט משוערת; טרם הוזמן'),
row('24','09:30','12:00','Tower of London','tower','tower','planned','Go City Explorer'),
row('24','12:15','13:15','Tower Bridge Experience','bridge','city','planned','Go City Explorer; נדרש שריון'),
row('24','13:30','14:25','Borough Market','borough','market'),
row('24','14:25','14:55','London Bridge','londonbridge','city'),
row('24','16:30','17:30','Madame Tussauds','tussauds','museum','booked','הזמנה קיימת לשעה 16:30'),
row('25','10:00','12:30','British Museum','british','museum','booked','הזמנה קיימת; לבדוק את שעת הכרטיס'),
row('25','12:45','15:30','Camden Market + Stables','camden','market'),
row('25','16:00','18:30','Covent Garden, Chinatown, Soho','covent','city'),
row('26','09:30','10:00','Wellington Arch','wellington','city'),
row('26','10:00','12:15','Buckingham Palace והחלפת המשמר','palace','city','planned','החלפת משמר רק אם מתקיימת באותו יום'),
row('26','12:30','14:00','Westminster Abbey','abbey','city','planned','כרטיס נפרד; לא הוזמן'),
row('26','14:00','16:00','Big Ben, Whitehall, Horse Guards','bigben','city'),
row('26','16:15','17:30','Frameless','frameless','museum','optional','הוצע כאטרקציה השישית; טרם אושר'),
row('27','10:00','11:45','Natural History Museum','natural','museum','booked','הזמנה קיימת; לבדוק שעת הכרטיס'),
row('27','12:00','13:15','Science Museum','science','museum','booked','הזמנה קיימת; לבדוק שעת הכרטיס'),
row('27','15:15','16:15','Sky Garden','sky','garden','booked','הזמנה מאושרת ל־15:15; סיום משוער'),
row('27','16:30','17:30','St Paul’s Cathedral','paul','city','optional','יש לוודא שניתן להיכנס בשעה זו'),
row('27','17:30','17:50','Millennium Bridge','millennium','city'),
row('27','18:05','18:35','Leadenhall Market','leadenhall','market'),
row('27','18:45','19:30','Horizon 22','horizon','garden','optional','אין כרטיס; יש לוודא שעות פעילות'),
row('28','10:00','11:30','Tottenham Stadium Tour','tottenham','football','planned','Explorer; השעה טרם אושרה'),
row('28','12:00','13:30','Dare Skywalk','tottenham','football','planned','Explorer; השעה טרם אושרה'),
row('28','13:30','14:30','ארוחת צהריים – טוטנהאם','tottenham','football')
];
let data;try{const stored=JSON.parse(localStorage.getItem('londontrip-events'));data=Array.isArray(stored)?stored:SEED}catch{data=SEED}

// Official attraction websites; where no reliable official page is known, no link is shown.
const OFFICIAL={
queens:'https://www.rmg.co.uk/queens-house',
greenwich:'https://www.greenwichmarket.london/',
meridian:'https://www.rmg.co.uk/royal-observatory',
tunnel:'https://www.royalgreenwich.gov.uk/',
pier:'https://www.thamesclippers.com/',
tower:'https://www.hrp.org.uk/tower-of-london/',
bridge:'https://www.towerbridge.org.uk/',
borough:'https://boroughmarket.org.uk/',
tussauds:'https://www.madametussauds.com/london/',
british:'https://www.britishmuseum.org/',
camden:'https://camdenmarket.com/',
covent:'https://www.coventgarden.london/',
wellington:'https://www.english-heritage.org.uk/visit/places/wellington-arch/',
palace:'https://www.rct.uk/visit/buckingham-palace',
abbey:'https://www.westminster-abbey.org/',
frameless:'https://frameless.com/',
natural:'https://www.nhm.ac.uk/',
science:'https://www.sciencemuseum.org.uk/',
sky:'https://skygarden.london/',
paul:'https://www.stpauls.co.uk/',
leadenhall:'https://leadenhallmarket.co.uk/',
horizon:'https://horizon22.co.uk/',
tottenham:'https://www.tottenhamhotspur.com/the-stadium/visitor-attractions/'
};
function officialUrl(x){
 const k=keyOf(x);
 if(k==='tottenham'&&/ארוחת/.test(x.title))return null;
 if(k==='tottenham'&&/Skywalk/i.test(x.title))return 'https://www.tottenhamhotspur.com/the-stadium/visitor-attractions/dare-skywalk/';
 if(k==='tottenham'&&/Tour/i.test(x.title))return 'https://www.tottenhamhotspur.com/the-stadium/visitor-attractions/stadium-tours/';
 return OFFICIAL[k]||null;
}
function openOfficial(id){const x=data.find(y=>y.id===id);const url=x&&officialUrl(x);if(url)window.open(url,'_blank','noopener,noreferrer')}

let current='23',editing=null,map=null,markers=[];
const esc=s=>String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const save=()=>localStorage.setItem('londontrip-events',JSON.stringify(data));
const todays=()=>data.filter(x=>x.day===current).sort((a,b)=>a.time.localeCompare(b.time));
function maps(x,mode='walking'){const q=x.coords?x.coords.join(','):x.place;window.open('https://www.google.com/maps/dir/?api=1&destination='+encodeURIComponent(q)+'&travelmode='+mode,'_blank','noopener')}
function routeDay(mode){const xs=todays().filter(x=>x.coords);if(!xs.length)return alert('אין מיקומים ליום הזה');const first=xs[0],last=xs[xs.length-1];const mids=xs.slice(1,-1).map(x=>x.coords.join(',')).join('|');let url='https://www.google.com/maps/dir/?api=1&origin='+first.coords.join(',')+'&destination='+last.coords.join(',')+'&travelmode='+mode;if(mids)url+='&waypoints='+encodeURIComponent(mids);window.open(url,'_blank','noopener')}
function render(){document.getElementById('days').innerHTML=DAYS.map(([d,name])=>'<button class="day '+(d===current?'active':'')+'" onclick="chooseDay(\''+d+'\')">'+name+'<br>'+d+'/10</button>').join('');let dd=DAYS.find(x=>x[0]===current);document.getElementById('dayName').textContent=dd[1]+' '+dd[0]+'/10';document.getElementById('count').textContent=todays().length+' פעילויות';document.getElementById('heading').textContent=dd[2];document.getElementById('cards').innerHTML=todays().map(x=>'<article class="panel"><div class="item"><img class="thumb" loading="lazy" src="'+esc(x.photo||pic('city'))+'" alt=""><div class="info"><div class="time">'+esc(x.time)+'–'+esc(x.end)+'</div><div class="title">'+(officialUrl(x)?'<a href="'+esc(officialUrl(x))+'" target="_blank" rel="noopener noreferrer" style="color:inherit;text-decoration:underline;text-decoration-color:#e9bd6e;text-underline-offset:4px">'+esc(x.title)+' ↗</a>':esc(x.title))+'</div><span class="tag">'+({booked:'✓ הוזמן',planned:'מתוכנן',optional:'אופציונלי'}[x.status]||'מתוכנן')+'</span><p class="muted">'+esc(x.note)+'</p></div></div><div class="actions">'+(officialUrl(x)?'<button onclick="openOfficial(\''+x.id+'\')">🌐 אתר רשמי</button>':'')+'<button onclick="navigateId(\''+x.id+'\')">📍 Google Maps</button><button onclick="editId(\''+x.id+'\')">✎ עריכה</button><button onclick="removeId(\''+x.id+'\')">🗑 מחיקה</button></div></article>').join('')||'<div class="panel">אין פעילויות ליום הזה. אפשר להוסיף פעילות חדשה.</div>';drawMap()}
function chooseDay(d){current=d;render()}
function navigateId(id){let x=data.find(x=>x.id===id);if(x)maps(x)}
function editId(id){editing=id;let x=data.find(x=>x.id===id);document.getElementById('editTitle').textContent='עריכת פעילות';openForm(x)}
function addEvent(){editing=null;document.getElementById('editTitle').textContent='פעילות חדשה';openForm({day:current,time:'10:00',end:'11:00',title:'',place:'',photo:'',status:'planned',note:''})}
function openForm(x){let f=document.getElementById('editform');document.getElementById('daySelect').innerHTML=DAYS.map(([d,n])=>'<option value="'+d+'">'+n+' '+d+'/10</option>').join('');for(let k of ['day','time','end','title','place','photo','status','note'])f.elements[k].value=x[k]||'';document.getElementById('editor').showModal()}
function saveEvent(e){e.preventDefault();let f=e.target,x=editing?data.find(x=>x.id===editing):{id:crypto.randomUUID(),coords:null};if(!x)return;let oldPlace=x.place;for(let k of ['day','time','end','title','place','photo','status','note'])x[k]=f.elements[k].value;if(!x.photo)x.photo=pic('city');if(x.place!==oldPlace)x.coords=null;if(!editing)data.push(x);current=x.day;save();document.getElementById('editor').close();render()}
function removeId(id){if(confirm('למחוק את הפעילות?')){data=data.filter(x=>x.id!==id);save();render()}}
function exportData(){const blob=new Blob([JSON.stringify({version:1,events:data},null,2)],{type:'application/json'});const url=URL.createObjectURL(blob);const a=document.createElement('a');a.href=url;a.download='london-trip-2026.json';a.click();setTimeout(()=>URL.revokeObjectURL(url),2000)}
async function importData(input){if(!input.files?.[0])return;try{const obj=JSON.parse(await input.files[0].text());if(!Array.isArray(obj.events))throw Error();if(!confirm('להחליף את המסלול הקיים במסלול המיובא?'))return;data=obj.events.filter(x=>x&&typeof x.title==='string'&&typeof x.day==='string');save();render()}catch{alert('קובץ לא תקין')}input.value=''}
function drawMap(){const el=document.getElementById('map');if(typeof L==='undefined'){el.innerHTML='<p style="padding:15px">המפה דורשת חיבור לאינטרנט. כפתורי Google Maps עדיין זמינים.</p>';return}if(!map){map=L.map(el).setView([51.51,-.12],11);L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{attribution:'© OpenStreetMap',maxZoom:19}).addTo(map)}markers.forEach(m=>map.removeLayer(m));markers=[];let xs=todays().filter(x=>Array.isArray(x.coords));xs.forEach((x,i)=>{let m=L.marker(x.coords).addTo(map).bindPopup('<b>'+esc(x.title)+'</b><br>'+esc(x.time));markers.push(m)});if(xs.length){let line=L.polyline(xs.map(x=>x.coords),{color:'#e9bd6e',weight:3}).addTo(map);markers.push(line);map.fitBounds(line.getBounds().pad(.25),{maxZoom:14})}else map.setView([51.51,-.12],11);setTimeout(()=>map.invalidateSize(),100)}
if('serviceWorker' in navigator)window.addEventListener('load',()=>navigator.serviceWorker.register('./sw.js').catch(()=>{}));

// Photos per attraction: Wikipedia page images (CC-licensed or Wikipedia-hosted), with distinct fallback.
const WIKI={
queens:"Queen's House",greenwich:"Greenwich Market",meridian:"Royal Observatory, Greenwich",tunnel:"Greenwich foot tunnel",pier:"Greenwich Pier",
tower:"Tower of London",bridge:"Tower Bridge",borough:"Borough Market",londonbridge:"London Bridge",tussauds:"Madame Tussauds London",
british:"British Museum",camden:"Camden Market",covent:"Covent Garden",wellington:"Wellington Arch",palace:"Buckingham Palace",
abbey:"Westminster Abbey",bigben:"Big Ben",frameless:"Frameless (art exhibition)",natural:"Natural History Museum, London",
science:"Science Museum, London",sky:"Sky Garden, London",paul:"St Paul's Cathedral",millennium:"Millennium Bridge, London",
leadenhall:"Leadenhall Market",horizon:"22 Bishopsgate",tottenham:"Tottenham Hotspur Stadium"};
const fallback={
queens:"https://commons.wikimedia.org/wiki/Special:FilePath/Queen%27s_House,_Greenwich-7354947198.jpg?width=500",
tunnel:"https://images.unsplash.com/photo-1533929736458-ca588d08c8be?w=500",
};
const keyOf=x=>{if(!Array.isArray(x.coords))return null;return Object.keys(P).find(k=>P[k][0]===x.coords[0]&&P[k][1]===x.coords[1])||null};
const originalPhotos=new Set(Object.values(photos).map(v=>'https://images.unsplash.com/'+v+'?w=640&q=75&fit=crop'));
const resolvedImages={};
async function updateAttractionPhotos(){
  const keys=[...new Set(data.map(keyOf).filter(Boolean))];
  for(let i=0;i<keys.length;i+=5){
    await Promise.all(keys.slice(i,i+5).map(async k=>{
      try{
        const q=new URLSearchParams({action:'query',format:'json',origin:'*',prop:'pageimages',pithumbsize:'600',redirects:'1',titles:WIKI[k]});
        const response=await fetch('https://en.wikipedia.org/w/api.php?'+q.toString());
        if(!response.ok)throw Error('photo lookup failed');
        const result=await response.json();
        const page=Object.values(result.query?.pages||{})[0];
        const url=page?.thumbnail?.source||fallback[k];
        if(url&&/^https:\/\//.test(url))resolvedImages[k]=url;
      }catch{if(fallback[k])resolvedImages[k]=fallback[k]}
    }));
    let changed=false;
    for(const x of data){
      const k=keyOf(x),url=k&&resolvedImages[k];
      if(url&&(originalPhotos.has(x.photo)||!x.photo||x.photo===pic('city'))&&x.photo!==url){x.photo=url;changed=true}
    }
    if(changed){save();render()}
  }
}
render();
updateAttractionPhotos();
