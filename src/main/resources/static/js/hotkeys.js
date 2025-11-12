(function(){
  if (window.__HK_INITED__) return; window.__HK_INITED__ = true;
  console.log('[hotkeys] script loaded');

  let wired = false, links = [], seqStage = 0, seqTimer = null;

  function findHeader(){
    const h = Array.from(document.querySelectorAll('.main-header'))
      .find(x => x.offsetParent !== null) || null;
    console.log('[hotkeys] findHeader ->', h);
    return h;
  }
  function collectLinks(h){
    const r = Array.from(h.querySelectorAll('a[href]'))
      .filter(a => a.offsetParent !== null && !a.getAttribute('href')?.startsWith('#'))
      .slice(0,8);
    console.log('[hotkeys] collected links:', r.map(a=>a.textContent.trim()));
    return r;
  }
  function badge(a, text){
    if (a.querySelector('.hk-badge')) return;
    const b = document.createElement('span');
    b.className = 'hk-badge';
    b.textContent = text;
    b.style.cssText = 'display:inline-block;margin-left:.35rem;padding:.1rem .3rem;border:1px solid #e5e7eb;border-radius:.4rem;font:12px/1.1 system-ui;background:#f9fafb;color:#374151;vertical-align:middle;';
    a.appendChild(b);
  }
  function showHelp(){
    if (document.querySelector('.hk-help')) return;
    const el = document.createElement('div');
    el.className = 'hk-help';
    el.style.cssText = 'position:fixed;right:16px;bottom:16px;background:#111827;color:#fff;padding:10px 12px;border-radius:10px;box-shadow:0 10px 20px rgba(0,0,0,.15);font:13px/1.3 system-ui;opacity:.9;';
    el.innerHTML = '<strong>Навигация хедера</strong><div style="margin-top:6px">Нажмите <kbd>g</kbd>, затем <kbd>1…8</kbd><br><small>Справка: <kbd>Shift</kbd>+<kbd>?</kbd></small></div>';
    document.body.appendChild(el);
  }

  function wire(){
    if (wired) return;
    const header = findHeader(); if (!header) { console.warn('[hotkeys] header not found yet'); return; }

    links = collectLinks(header);
    if (!links.length) { console.warn('[hotkeys] no links in header'); return; }

    links.forEach((a,i)=>badge(a, `g,${i+1}`));
    showHelp();

    document.addEventListener('keydown', (e)=>{
      // help toggle
      if (e.shiftKey && (e.key === '?' || e.key === '/')) {
        e.preventDefault();
        const box = document.querySelector('.hk-help');
        if (box) box.style.display = (box.style.display === 'none') ? '' : 'none';
        return;
      }
      const tag = (e.target.tagName||'').toLowerCase();
      if (['input','textarea','select'].includes(tag) || e.target.isContentEditable) return;

      // двухударная: g, затем цифра
      if (seqStage === 0) {
        if (e.key.toLowerCase() === 'g' && !e.ctrlKey && !e.altKey && !e.metaKey) {
          e.preventDefault();
          seqStage = 1;
          clearTimeout(seqTimer);
          seqTimer = setTimeout(()=> seqStage=0, 1500);
        }
      } else {
        const d = Number(e.key);
        if (Number.isInteger(d) && d>=1 && d<=links.length) {
          e.preventDefault();
          clearTimeout(seqTimer);
          seqStage = 0;
          links[d-1].focus(); links[d-1].click();
        } else {
          seqStage = 0;
        }
      }
    }, {passive:false});

    wired = true;
    console.log('[hotkeys] wired OK');
  }

  // запускаем после полной разметки
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', wire);
  } else {
    wire();
  }
  // если фрагмент появится чуть позже — дёрнем ещё раз
  const mo = new MutationObserver(()=>{ if (!wired) wire(); });
  mo.observe(document.documentElement, {childList:true, subtree:true});
})();
