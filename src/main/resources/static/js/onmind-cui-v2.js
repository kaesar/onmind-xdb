const e=globalThis,t=e.ShadowRoot&&(void 0===e.ShadyCSS||e.ShadyCSS.nativeShadow)&&"adoptedStyleSheets"in Document.prototype&&"replace"in CSSStyleSheet.prototype,o=Symbol(),r=/* @__PURE__ */new WeakMap;let i=class{constructor(e,t,r){if(this._$cssResult$=!0,r!==o)throw Error("CSSResult is not constructable. Use `unsafeCSS` or `css` instead.");this.cssText=e,this.t=t}get styleSheet(){let e=this.o;const o=this.t;if(t&&void 0===e){const t=void 0!==o&&1===o.length;t&&(e=r.get(o)),void 0===e&&((this.o=e=new CSSStyleSheet).replaceSync(this.cssText),t&&r.set(o,e))}return e}toString(){return this.cssText}};const s=(e,...t)=>{const r=1===e.length?e[0]:t.reduce((t,o,r)=>t+(e=>{if(!0===e._$cssResult$)return e.cssText;if("number"==typeof e)return e;throw Error("Value passed to 'css' function must be a 'css' function result: "+e+". Use 'unsafeCSS' to pass non-literal values, but take care to ensure page security.")})(o)+e[r+1],e[0]);return new i(r,e,o)},a=t?e=>e:e=>e instanceof CSSStyleSheet?(e=>{let t="";for(const o of e.cssRules)t+=o.cssText;return(e=>new i("string"==typeof e?e:e+"",void 0,o))(t)})(e):e,{is:l,defineProperty:n,getOwnPropertyDescriptor:d,getOwnPropertyNames:h,getOwnPropertySymbols:c,getPrototypeOf:p}=Object,u=globalThis,m=u.trustedTypes,b=m?m.emptyScript:"",f=u.reactiveElementPolyfillSupport,g=(e,t)=>e,v={toAttribute(e,t){switch(t){case Boolean:e=e?b:null;break;case Object:case Array:e=null==e?e:JSON.stringify(e)}return e},fromAttribute(e,t){let o=e;switch(t){case Boolean:o=null!==e;break;case Number:o=null===e?null:Number(e);break;case Object:case Array:try{o=JSON.parse(e)}catch(r){o=null}}return o}},y=(e,t)=>!l(e,t),$={attribute:!0,type:String,converter:v,reflect:!1,useDefault:!1,hasChanged:y};Symbol.metadata??=Symbol("metadata"),u.litPropertyMetadata??=/* @__PURE__ */new WeakMap;let _=class extends HTMLElement{static addInitializer(e){this._$Ei(),(this.l??=[]).push(e)}static get observedAttributes(){return this.finalize(),this._$Eh&&[...this._$Eh.keys()]}static createProperty(e,t=$){if(t.state&&(t.attribute=!1),this._$Ei(),this.prototype.hasOwnProperty(e)&&((t=Object.create(t)).wrapped=!0),this.elementProperties.set(e,t),!t.noAccessor){const o=Symbol(),r=this.getPropertyDescriptor(e,o,t);void 0!==r&&n(this.prototype,e,r)}}static getPropertyDescriptor(e,t,o){const{get:r,set:i}=d(this.prototype,e)??{get(){return this[t]},set(e){this[t]=e}};return{get:r,set(t){const s=r?.call(this);i?.call(this,t),this.requestUpdate(e,s,o)},configurable:!0,enumerable:!0}}static getPropertyOptions(e){return this.elementProperties.get(e)??$}static _$Ei(){if(this.hasOwnProperty(g("elementProperties")))return;const e=p(this);e.finalize(),void 0!==e.l&&(this.l=[...e.l]),this.elementProperties=new Map(e.elementProperties)}static finalize(){if(this.hasOwnProperty(g("finalized")))return;if(this.finalized=!0,this._$Ei(),this.hasOwnProperty(g("properties"))){const e=this.properties,t=[...h(e),...c(e)];for(const o of t)this.createProperty(o,e[o])}const e=this[Symbol.metadata];if(null!==e){const t=litPropertyMetadata.get(e);if(void 0!==t)for(const[e,o]of t)this.elementProperties.set(e,o)}this._$Eh=/* @__PURE__ */new Map;for(const[t,o]of this.elementProperties){const e=this._$Eu(t,o);void 0!==e&&this._$Eh.set(e,t)}this.elementStyles=this.finalizeStyles(this.styles)}static finalizeStyles(e){const t=[];if(Array.isArray(e)){const o=new Set(e.flat(1/0).reverse());for(const e of o)t.unshift(a(e))}else void 0!==e&&t.push(a(e));return t}static _$Eu(e,t){const o=t.attribute;return!1===o?void 0:"string"==typeof o?o:"string"==typeof e?e.toLowerCase():void 0}constructor(){super(),this._$Ep=void 0,this.isUpdatePending=!1,this.hasUpdated=!1,this._$Em=null,this._$Ev()}_$Ev(){this._$ES=new Promise(e=>this.enableUpdating=e),this._$AL=/* @__PURE__ */new Map,this._$E_(),this.requestUpdate(),this.constructor.l?.forEach(e=>e(this))}addController(e){(this._$EO??=/* @__PURE__ */new Set).add(e),void 0!==this.renderRoot&&this.isConnected&&e.hostConnected?.()}removeController(e){this._$EO?.delete(e)}_$E_(){const e=/* @__PURE__ */new Map,t=this.constructor.elementProperties;for(const o of t.keys())this.hasOwnProperty(o)&&(e.set(o,this[o]),delete this[o]);e.size>0&&(this._$Ep=e)}createRenderRoot(){const o=this.shadowRoot??this.attachShadow(this.constructor.shadowRootOptions);return((o,r)=>{if(t)o.adoptedStyleSheets=r.map(e=>e instanceof CSSStyleSheet?e:e.styleSheet);else for(const t of r){const r=document.createElement("style"),i=e.litNonce;void 0!==i&&r.setAttribute("nonce",i),r.textContent=t.cssText,o.appendChild(r)}})(o,this.constructor.elementStyles),o}connectedCallback(){this.renderRoot??=this.createRenderRoot(),this.enableUpdating(!0),this._$EO?.forEach(e=>e.hostConnected?.())}enableUpdating(e){}disconnectedCallback(){this._$EO?.forEach(e=>e.hostDisconnected?.())}attributeChangedCallback(e,t,o){this._$AK(e,o)}_$ET(e,t){const o=this.constructor.elementProperties.get(e),r=this.constructor._$Eu(e,o);if(void 0!==r&&!0===o.reflect){const i=(void 0!==o.converter?.toAttribute?o.converter:v).toAttribute(t,o.type);this._$Em=e,null==i?this.removeAttribute(r):this.setAttribute(r,i),this._$Em=null}}_$AK(e,t){const o=this.constructor,r=o._$Eh.get(e);if(void 0!==r&&this._$Em!==r){const e=o.getPropertyOptions(r),i="function"==typeof e.converter?{fromAttribute:e.converter}:void 0!==e.converter?.fromAttribute?e.converter:v;this._$Em=r;const s=i.fromAttribute(t,e.type);this[r]=s??this._$Ej?.get(r)??s,this._$Em=null}}requestUpdate(e,t,o){if(void 0!==e){const r=this.constructor,i=this[e];if(o??=r.getPropertyOptions(e),!((o.hasChanged??y)(i,t)||o.useDefault&&o.reflect&&i===this._$Ej?.get(e)&&!this.hasAttribute(r._$Eu(e,o))))return;this.C(e,t,o)}!1===this.isUpdatePending&&(this._$ES=this._$EP())}C(e,t,{useDefault:o,reflect:r,wrapped:i},s){o&&!(this._$Ej??=/* @__PURE__ */new Map).has(e)&&(this._$Ej.set(e,s??t??this[e]),!0!==i||void 0!==s)||(this._$AL.has(e)||(this.hasUpdated||o||(t=void 0),this._$AL.set(e,t)),!0===r&&this._$Em!==e&&(this._$Eq??=/* @__PURE__ */new Set).add(e))}async _$EP(){this.isUpdatePending=!0;try{await this._$ES}catch(t){Promise.reject(t)}const e=this.scheduleUpdate();return null!=e&&await e,!this.isUpdatePending}scheduleUpdate(){return this.performUpdate()}performUpdate(){if(!this.isUpdatePending)return;if(!this.hasUpdated){if(this.renderRoot??=this.createRenderRoot(),this._$Ep){for(const[e,t]of this._$Ep)this[e]=t;this._$Ep=void 0}const e=this.constructor.elementProperties;if(e.size>0)for(const[t,o]of e){const{wrapped:e}=o,r=this[t];!0!==e||this._$AL.has(t)||void 0===r||this.C(t,void 0,o,r)}}let e=!1;const t=this._$AL;try{e=this.shouldUpdate(t),e?(this.willUpdate(t),this._$EO?.forEach(e=>e.hostUpdate?.()),this.update(t)):this._$EM()}catch(o){throw e=!1,this._$EM(),o}e&&this._$AE(t)}willUpdate(e){}_$AE(e){this._$EO?.forEach(e=>e.hostUpdated?.()),this.hasUpdated||(this.hasUpdated=!0,this.firstUpdated(e)),this.updated(e)}_$EM(){this._$AL=/* @__PURE__ */new Map,this.isUpdatePending=!1}get updateComplete(){return this.getUpdateComplete()}getUpdateComplete(){return this._$ES}shouldUpdate(e){return!0}update(e){this._$Eq&&=this._$Eq.forEach(e=>this._$ET(e,this[e])),this._$EM()}updated(e){}firstUpdated(e){}};_.elementStyles=[],_.shadowRootOptions={mode:"open"},_[g("elementProperties")]=/* @__PURE__ */new Map,_[g("finalized")]=/* @__PURE__ */new Map,f?.({ReactiveElement:_}),(u.reactiveElementVersions??=[]).push("2.1.1");const x=globalThis,w=x.trustedTypes,k=w?w.createPolicy("lit-html",{createHTML:e=>e}):void 0,S="$lit$",A=`lit$${Math.random().toFixed(9).slice(2)}$`,C="?"+A,E=`<${C}>`,O=document,M=()=>O.createComment(""),D=e=>null===e||"object"!=typeof e&&"function"!=typeof e,P=Array.isArray,z="[ \t\n\f\r]",j=/<(?:(!--|\/[^a-zA-Z])|(\/?[a-zA-Z][^>\s]*)|(\/?$))/g,B=/-->/g,U=/>/g,N=RegExp(`>|${z}(?:([^\\s"'>=/]+)(${z}*=${z}*(?:[^ \t\n\f\r"'\`<>=]|("|')|))|$)`,"g"),F=/'/g,R=/"/g,H=/^(?:script|style|textarea|title)$/i,T=(V=1,(e,...t)=>({_$litType$:V,strings:e,values:t})),I=Symbol.for("lit-noChange"),q=Symbol.for("lit-nothing"),L=/* @__PURE__ */new WeakMap,W=O.createTreeWalker(O,129);var V;function K(e,t){if(!P(e)||!e.hasOwnProperty("raw"))throw Error("invalid template strings array");return void 0!==k?k.createHTML(t):t}class J{constructor({strings:e,_$litType$:t},o){let r;this.parts=[];let i=0,s=0;const a=e.length-1,l=this.parts,[n,d]=((e,t)=>{const o=e.length-1,r=[];let i,s=2===t?"<svg>":3===t?"<math>":"",a=j;for(let l=0;l<o;l++){const t=e[l];let o,n,d=-1,h=0;for(;h<t.length&&(a.lastIndex=h,n=a.exec(t),null!==n);)h=a.lastIndex,a===j?"!--"===n[1]?a=B:void 0!==n[1]?a=U:void 0!==n[2]?(H.test(n[2])&&(i=RegExp("</"+n[2],"g")),a=N):void 0!==n[3]&&(a=N):a===N?">"===n[0]?(a=i??j,d=-1):void 0===n[1]?d=-2:(d=a.lastIndex-n[2].length,o=n[1],a=void 0===n[3]?N:'"'===n[3]?R:F):a===R||a===F?a=N:a===B||a===U?a=j:(a=N,i=void 0);const c=a===N&&e[l+1].startsWith("/>")?" ":"";s+=a===j?t+E:d>=0?(r.push(o),t.slice(0,d)+S+t.slice(d)+A+c):t+A+(-2===d?l:c)}return[K(e,s+(e[o]||"<?>")+(2===t?"</svg>":3===t?"</math>":"")),r]})(e,t);if(this.el=J.createElement(n,o),W.currentNode=this.el.content,2===t||3===t){const e=this.el.content.firstChild;e.replaceWith(...e.childNodes)}for(;null!==(r=W.nextNode())&&l.length<a;){if(1===r.nodeType){if(r.hasAttributes())for(const e of r.getAttributeNames())if(e.endsWith(S)){const t=d[s++],o=r.getAttribute(e).split(A),a=/([.?@])?(.*)/.exec(t);l.push({type:1,index:i,name:a[2],strings:o,ctor:"."===a[1]?Q:"?"===a[1]?ee:"@"===a[1]?te:G}),r.removeAttribute(e)}else e.startsWith(A)&&(l.push({type:6,index:i}),r.removeAttribute(e));if(H.test(r.tagName)){const e=r.textContent.split(A),t=e.length-1;if(t>0){r.textContent=w?w.emptyScript:"";for(let o=0;o<t;o++)r.append(e[o],M()),W.nextNode(),l.push({type:2,index:++i});r.append(e[t],M())}}}else if(8===r.nodeType)if(r.data===C)l.push({type:2,index:i});else{let e=-1;for(;-1!==(e=r.data.indexOf(A,e+1));)l.push({type:7,index:i}),e+=A.length-1}i++}}static createElement(e,t){const o=O.createElement("template");return o.innerHTML=e,o}}function Y(e,t,o=e,r){if(t===I)return t;let i=void 0!==r?o._$Co?.[r]:o._$Cl;const s=D(t)?void 0:t._$litDirective$;return i?.constructor!==s&&(i?._$AO?.(!1),void 0===s?i=void 0:(i=new s(e),i._$AT(e,o,r)),void 0!==r?(o._$Co??=[])[r]=i:o._$Cl=i),void 0!==i&&(t=Y(e,i._$AS(e,t.values),i,r)),t}class X{constructor(e,t){this._$AV=[],this._$AN=void 0,this._$AD=e,this._$AM=t}get parentNode(){return this._$AM.parentNode}get _$AU(){return this._$AM._$AU}u(e){const{el:{content:t},parts:o}=this._$AD,r=(e?.creationScope??O).importNode(t,!0);W.currentNode=r;let i=W.nextNode(),s=0,a=0,l=o[0];for(;void 0!==l;){if(s===l.index){let t;2===l.type?t=new Z(i,i.nextSibling,this,e):1===l.type?t=new l.ctor(i,l.name,l.strings,this,e):6===l.type&&(t=new oe(i,this,e)),this._$AV.push(t),l=o[++a]}s!==l?.index&&(i=W.nextNode(),s++)}return W.currentNode=O,r}p(e){let t=0;for(const o of this._$AV)void 0!==o&&(void 0!==o.strings?(o._$AI(e,o,t),t+=o.strings.length-2):o._$AI(e[t])),t++}}class Z{get _$AU(){return this._$AM?._$AU??this._$Cv}constructor(e,t,o,r){this.type=2,this._$AH=q,this._$AN=void 0,this._$AA=e,this._$AB=t,this._$AM=o,this.options=r,this._$Cv=r?.isConnected??!0}get parentNode(){let e=this._$AA.parentNode;const t=this._$AM;return void 0!==t&&11===e?.nodeType&&(e=t.parentNode),e}get startNode(){return this._$AA}get endNode(){return this._$AB}_$AI(e,t=this){e=Y(this,e,t),D(e)?e===q||null==e||""===e?(this._$AH!==q&&this._$AR(),this._$AH=q):e!==this._$AH&&e!==I&&this._(e):void 0!==e._$litType$?this.$(e):void 0!==e.nodeType?this.T(e):(e=>P(e)||"function"==typeof e?.[Symbol.iterator])(e)?this.k(e):this._(e)}O(e){return this._$AA.parentNode.insertBefore(e,this._$AB)}T(e){this._$AH!==e&&(this._$AR(),this._$AH=this.O(e))}_(e){this._$AH!==q&&D(this._$AH)?this._$AA.nextSibling.data=e:this.T(O.createTextNode(e)),this._$AH=e}$(e){const{values:t,_$litType$:o}=e,r="number"==typeof o?this._$AC(e):(void 0===o.el&&(o.el=J.createElement(K(o.h,o.h[0]),this.options)),o);if(this._$AH?._$AD===r)this._$AH.p(t);else{const e=new X(r,this),o=e.u(this.options);e.p(t),this.T(o),this._$AH=e}}_$AC(e){let t=L.get(e.strings);return void 0===t&&L.set(e.strings,t=new J(e)),t}k(e){P(this._$AH)||(this._$AH=[],this._$AR());const t=this._$AH;let o,r=0;for(const i of e)r===t.length?t.push(o=new Z(this.O(M()),this.O(M()),this,this.options)):o=t[r],o._$AI(i),r++;r<t.length&&(this._$AR(o&&o._$AB.nextSibling,r),t.length=r)}_$AR(e=this._$AA.nextSibling,t){for(this._$AP?.(!1,!0,t);e!==this._$AB;){const t=e.nextSibling;e.remove(),e=t}}setConnected(e){void 0===this._$AM&&(this._$Cv=e,this._$AP?.(e))}}class G{get tagName(){return this.element.tagName}get _$AU(){return this._$AM._$AU}constructor(e,t,o,r,i){this.type=1,this._$AH=q,this._$AN=void 0,this.element=e,this.name=t,this._$AM=r,this.options=i,o.length>2||""!==o[0]||""!==o[1]?(this._$AH=Array(o.length-1).fill(new String),this.strings=o):this._$AH=q}_$AI(e,t=this,o,r){const i=this.strings;let s=!1;if(void 0===i)e=Y(this,e,t,0),s=!D(e)||e!==this._$AH&&e!==I,s&&(this._$AH=e);else{const r=e;let a,l;for(e=i[0],a=0;a<i.length-1;a++)l=Y(this,r[o+a],t,a),l===I&&(l=this._$AH[a]),s||=!D(l)||l!==this._$AH[a],l===q?e=q:e!==q&&(e+=(l??"")+i[a+1]),this._$AH[a]=l}s&&!r&&this.j(e)}j(e){e===q?this.element.removeAttribute(this.name):this.element.setAttribute(this.name,e??"")}}class Q extends G{constructor(){super(...arguments),this.type=3}j(e){this.element[this.name]=e===q?void 0:e}}class ee extends G{constructor(){super(...arguments),this.type=4}j(e){this.element.toggleAttribute(this.name,!!e&&e!==q)}}class te extends G{constructor(e,t,o,r,i){super(e,t,o,r,i),this.type=5}_$AI(e,t=this){if((e=Y(this,e,t,0)??q)===I)return;const o=this._$AH,r=e===q&&o!==q||e.capture!==o.capture||e.once!==o.once||e.passive!==o.passive,i=e!==q&&(o===q||r);r&&this.element.removeEventListener(this.name,this,o),i&&this.element.addEventListener(this.name,this,e),this._$AH=e}handleEvent(e){"function"==typeof this._$AH?this._$AH.call(this.options?.host??this.element,e):this._$AH.handleEvent(e)}}class oe{constructor(e,t,o){this.element=e,this.type=6,this._$AN=void 0,this._$AM=t,this.options=o}get _$AU(){return this._$AM._$AU}_$AI(e){Y(this,e)}}const re=x.litHtmlPolyfillSupport;re?.(J,Z),(x.litHtmlVersions??=[]).push("3.3.1");const ie=globalThis;class se extends _{constructor(){super(...arguments),this.renderOptions={host:this},this._$Do=void 0}createRenderRoot(){const e=super.createRenderRoot();return this.renderOptions.renderBefore??=e.firstChild,e}update(e){const t=this.render();this.hasUpdated||(this.renderOptions.isConnected=this.isConnected),super.update(e),this._$Do=((e,t,o)=>{const r=o?.renderBefore??t;let i=r._$litPart$;if(void 0===i){const e=o?.renderBefore??null;r._$litPart$=i=new Z(t.insertBefore(M(),e),e,void 0,o??{})}return i._$AI(e),i})(t,this.renderRoot,this.renderOptions)}connectedCallback(){super.connectedCallback(),this._$Do?.setConnected(!0)}disconnectedCallback(){super.disconnectedCallback(),this._$Do?.setConnected(!1)}render(){return I}}se._$litElement$=!0,se.finalized=!0,ie.litElementHydrateSupport?.({LitElement:se});const ae=ie.litElementPolyfillSupport;ae?.({LitElement:se}),(ie.litElementVersions??=[]).push("4.2.1");const le=e=>(t,o)=>{void 0!==o?o.addInitializer(()=>{customElements.define(e,t)}):customElements.define(e,t)},ne={attribute:!0,type:String,converter:v,reflect:!1,hasChanged:y},de=(e=ne,t,o)=>{const{kind:r,metadata:i}=o;let s=globalThis.litPropertyMetadata.get(i);if(void 0===s&&globalThis.litPropertyMetadata.set(i,s=/* @__PURE__ */new Map),"setter"===r&&((e=Object.create(e)).wrapped=!0),s.set(o.name,e),"accessor"===r){const{name:r}=o;return{set(o){const i=t.get.call(this);t.set.call(this,o),this.requestUpdate(r,i,e)},init(t){return void 0!==t&&this.C(r,void 0,e,t),t}}}if("setter"===r){const{name:r}=o;return function(o){const i=this[r];t.call(this,o),this.requestUpdate(r,i,e)}}throw Error("Unsupported decorator location: "+r)};function he(e){return(t,o)=>"object"==typeof o?de(e,t,o):((e,t,o)=>{const r=t.hasOwnProperty(o);return t.constructor.createProperty(o,e),r?Object.getOwnPropertyDescriptor(t,o):void 0})(e,t,o)}function ce(e){return he({...e,state:!0,attribute:!1})}var pe=Object.defineProperty,ue=Object.getOwnPropertyDescriptor,me=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?ue(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&pe(t,o,s),s};let be=class extends se{constructor(){super(...arguments),this.dim="false",this.theme="light"}render(){const e="true"===this.dim,t="dark"===this.theme;let o="whitesmoke",r="0 3px 10px 0 #aaa",i="#1f2937";return t&&e?(o="#1f2937",r="0 3px 10px 0 #000",i="#f3f4f6"):t?(o="#374151",r="0 3px 10px 0 #000",i="#f3f4f6"):e&&(o="silver"),T`
            <div class="box" style="background-color: ${o}; box-shadow: ${r}; color: ${i};">
                <slot @slotchange="${this._handleSlotChange}"></slot>
            </div>`}_handleSlotChange(e){e.target.assignedElements().forEach(e=>{"dark"===this.theme&&e.setAttribute("theme","dark")})}};be.styles=s`
      :host {
        display: block;
      }
      .box {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
        padding: 1rem;
        border-radius: 5px;
      }
    `,me([he({type:String})],be.prototype,"dim",2),me([he({type:String})],be.prototype,"theme",2),be=me([le("as-box")],be);var fe=Object.defineProperty,ge=Object.getOwnPropertyDescriptor,ve=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?ge(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&fe(t,o,s),s};let ye=class extends se{constructor(){super(...arguments),this.label="Oops!",this.link="",this.message="",this.variant="primary",this.disabled=!1}render(){return T`
      <button 
        class="${this.variant}" 
        ?disabled="${this.disabled}"
        @click=${this.onClick}
      >
        ${this.label}
      </button>`}onClick(){this.disabled||(this.link?location.assign(this.link):this.message?this.showNotification(this.message):this.dispatchEvent(new CustomEvent("button-tap",{bubbles:!0,composed:!0})))}showNotification(e){const t=document.createElement("div");t.textContent=e,t.style.cssText="position:fixed;bottom:20px;left:50%;transform:translateX(-50%);background:#1f2937;color:white;padding:0.75rem 1.5rem;border-radius:4px;box-shadow:0 4px 6px rgba(0,0,0,0.1);z-index:9999;",document.body.appendChild(t),setTimeout(()=>t.remove(),3500)}};ye.styles=s`
    button {
      padding: 0.5rem 1rem;
      border: none;
      border-radius: 4px;
      font-size: 0.9375rem;
      font-weight: 500;
      font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif;
      cursor: pointer;
      transition: background 0.15s;
    }
    button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    .primary {
      background: #3b82f6;
      color: white;
    }
    .primary:hover:not(:disabled) {
      background: #2563eb;
    }
    .primary:active:not(:disabled) {
      background: #1d4ed8;
    }
    .secondary {
      background: #e5e7eb;
      color: #1f2937;
    }
    .secondary:hover:not(:disabled) {
      background: #d1d5db;
    }
    .secondary:active:not(:disabled) {
      background: #9ca3af;
    }
  `,ve([he({type:String})],ye.prototype,"label",2),ve([he({type:String})],ye.prototype,"link",2),ve([he({type:String})],ye.prototype,"message",2),ve([he({type:String})],ye.prototype,"variant",2),ve([he({type:Boolean})],ye.prototype,"disabled",2),ye=ve([le("as-button")],ye);var $e=Object.defineProperty,_e=Object.getOwnPropertyDescriptor,xe=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?_e(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&$e(t,o,s),s};let we=class extends se{constructor(){super(...arguments),this.label="",this.value="",this.placeholder=this.label,this.kind="text",this.theme="",this.readonly=!1,this.disabled=!1}render(){const e="text"===this.kind?"text":"email"===this.kind?"email":"password"===this.kind?"password":"number"===this.kind?"number":"text";return T`
          <div class="field">
            ${this.label?T`<label>${this.label}</label>`:""}
            <input
              type="${e}"
              .value="${this.value}"
              placeholder="${this.placeholder}"
              ?readonly=${this.readonly}
              ?disabled=${this.disabled}
              @input="${e=>{this.value=e.target.value,this.dispatchEvent(new CustomEvent("value-changed",{detail:{value:this.value},bubbles:!0,composed:!0}))}}"
            />
          </div>
        `}};we.styles=s`
      :host {
        display: block;
        font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif;
      }
      .field {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
      }
      label {
        font-size: 0.875rem;
        font-weight: 500;
        color: var(--label-color, #374151);
      }
      input {
        padding: 0.5rem 0.75rem;
        border: 1px solid transparent;
        border-radius: 4px;
        font-size: 0.9375rem;
        font-family: inherit;
        background: var(--input-bg, #e8eaed);
        color: var(--text-color, #1a1a1a);
        outline: none;
        transition: border-color 0.15s;
      }
      input:focus {
        border-color: var(--focus-color, #1676f3);
      }
      input::placeholder {
        color: var(--placeholder-color, #737373);
      }
      :host([theme="dark"]) {
        --label-color: #f3f4f6;
        --border-color: #525252;
        --input-bg: #374151;
        --text-color: #f3f4f6;
        --placeholder-color: #9ca3af;
        --focus-color: #1676f3;
      }
    `,xe([he({type:String})],we.prototype,"label",2),xe([he({type:String})],we.prototype,"value",2),xe([he({type:String})],we.prototype,"placeholder",2),xe([he({type:String})],we.prototype,"kind",2),xe([he({type:String})],we.prototype,"theme",2),xe([he({type:Boolean,reflect:!0})],we.prototype,"readonly",2),xe([he({type:Boolean,reflect:!0})],we.prototype,"disabled",2),we=xe([le("as-input")],we);var ke=Object.defineProperty,Se=Object.getOwnPropertyDescriptor,Ae=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?Se(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&ke(t,o,s),s};let Ce=class extends se{constructor(){super(...arguments),this.label="",this.checked=!1,this.theme="",this.readonly=!1,this.disabled=!1}render(){return T`
          <input
            type="checkbox"
            .checked="${this.checked}"
            ?disabled=${this.disabled}
            ?readonly=${this.readonly}
            @change="${e=>{this.readonly?e.target.checked=this.checked:(this.checked=e.target.checked,this.dispatchEvent(new CustomEvent("checked-changed",{detail:{value:this.checked},bubbles:!0,composed:!0})))}}"
          />
          ${this.label?T`<label>${this.label}</label>`:""}
        `}};Ce.styles=s`
      :host {
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
        cursor: pointer;
        font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif;
        font-size: 0.9375rem;
        color: var(--text-color, #1f2937);
      }
      input[type="checkbox"] {
        width: 18px;
        height: 18px;
        cursor: pointer;
        accent-color: var(--accent-color, #1676f3);
        appearance: none;
        -webkit-appearance: none;
        border: 2px solid var(--checkbox-border, #d0d7de);
        border-radius: 3px;
        background: var(--checkbox-bg, #fafafa);
        position: relative;
      }
      input[type="checkbox"]:checked {
        background: var(--accent-color, #1676f3);
        border-color: var(--accent-color, #1676f3);
      }
      input[type="checkbox"]:checked::after {
        content: '';
        position: absolute;
        left: 4px;
        top: 1px;
        width: 4px;
        height: 8px;
        border: solid white;
        border-width: 0 2px 2px 0;
        transform: rotate(45deg);
      }
      :host([theme="dark"]) {
        --text-color: #e5e5e5;
        --accent-color: #1676f3;
      }
    `,Ae([he({type:String})],Ce.prototype,"label",2),Ae([he({type:Boolean})],Ce.prototype,"checked",2),Ae([he({type:String})],Ce.prototype,"theme",2),Ae([he({type:Boolean,reflect:!0})],Ce.prototype,"readonly",2),Ae([he({type:Boolean,reflect:!0})],Ce.prototype,"disabled",2),Ce=Ae([le("as-check")],Ce);var Ee=Object.defineProperty,Oe=Object.getOwnPropertyDescriptor,Me=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?Oe(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&Ee(t,o,s),s};let De=class extends se{constructor(){super(...arguments),this.label="",this.checked=!1,this.theme="",this.readonly=!1,this.disabled=!1}render(){return T`
          <div
            class="switch ${this.checked?"checked":""}"
            ?aria-disabled=${this.disabled}
            @click="${()=>{this.disabled||this.readonly||(this.checked=!this.checked,this.dispatchEvent(new CustomEvent("checked-changed",{detail:{value:this.checked},bubbles:!0,composed:!0})))}}"
          ></div>
          ${this.label?T`<label>${this.label}</label>`:""}
        `}};De.styles=s`
      :host {
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
        cursor: pointer;
        font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif;
        font-size: 0.9375rem;
        color: var(--text-color, #1f2937);
      }
      .switch {
        position: relative;
        width: 36px;
        height: 20px;
        background: var(--switch-bg-off, #e8eaed);
        border-radius: 10px;
        transition: background 0.2s;
        cursor: pointer;
      }
      .switch.checked {
        background: var(--switch-bg-on, #1676f3);
      }
      .switch::after {
        content: '';
        position: absolute;
        width: 16px;
        height: 16px;
        border-radius: 50%;
        background: white;
        top: 2px;
        left: 2px;
        transition: transform 0.2s;
      }
      .switch.checked::after {
        transform: translateX(16px);
      }
      :host([theme="dark"]) {
        --text-color: #e5e5e5;
        --switch-bg-off: #525252;
        --switch-bg-on: #1676f3;
      }
    `,Me([he({type:String})],De.prototype,"label",2),Me([he({type:Boolean})],De.prototype,"checked",2),Me([he({type:String})],De.prototype,"theme",2),Me([he({type:Boolean,reflect:!0})],De.prototype,"readonly",2),Me([he({type:Boolean,reflect:!0})],De.prototype,"disabled",2),De=Me([le("as-switch")],De);var Pe,ze=Object.defineProperty,je=Object.getOwnPropertyDescriptor,Be=e=>{throw TypeError(e)},Ue=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?je(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&ze(t,o,s),s},Ne=(e,t,o)=>t.has(e)||Be("Cannot "+o);let Fe=class extends se{constructor(){var e,t,o;super(...arguments),this.label="Oops!",this.link="",this.message="",e=this,o=!1,(t=Pe).has(e)?Be("Cannot add the same private member more than once"):t instanceof WeakSet?t.add(e):t.set(e,o)}get dialogOpened(){return Ne(e=this,t=Pe,"read from private field"),o?o.call(e):t.get(e);var e,t,o}set dialogOpened(e){var t,o,r;r=e,Ne(t=this,o=Pe,"write to private field"),o.set(t,r)}render(){return T`
          <button @click=${this.open}>${this.label}</button>
          ${this.dialogOpened?T`
            <div class="overlay" @click=${this.close}>
              <div class="dialog" @click=${e=>e.stopPropagation()}>
                <div class="dialog-header">Confirm ?</div>
                <div class="dialog-content">${this.message}</div>
                <div class="dialog-actions">
                  <button class="btn-cancel" @click=${this.close}>Cancel</button>
                  <button class="btn-confirm" @click=${this.onClick}>${this.label}</button>
                </div>
              </div>
            </div>
          `:""}
        `}open(){this.dialogOpened=!0}close(){this.dialogOpened=!1}onClick(){console.log("Confirmed!"),this.dialogOpened=!1,this.link&&location.assign(this.link),this.dispatchEvent(new CustomEvent("confirm-tap",{bubbles:!0,composed:!0}))}};Pe=/* @__PURE__ */new WeakMap,Fe.styles=s`
      button {
        padding: 0.5rem 1rem;
        background: #3b82f6;
        color: white;
        border: none;
        border-radius: 4px;
        font-size: 0.9375rem;
        font-weight: 500;
        font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif;
        cursor: pointer;
        transition: background 0.15s;
      }
      button:hover {
        background: #2563eb;
      }
      .overlay {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0,0,0,0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 9999;
      }
      .dialog {
        background: white;
        border-radius: 8px;
        padding: 1.5rem;
        min-width: 300px;
        max-width: 500px;
        box-shadow: 0 10px 25px rgba(0,0,0,0.2);
        font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif;
      }
      .dialog-header {
        font-size: 1.125rem;
        font-weight: 600;
        margin-bottom: 1rem;
        color: #1f2937;
      }
      .dialog-content {
        margin-bottom: 1.5rem;
        color: #4b5563;
        font-size: 0.9375rem;
      }
      .dialog-actions {
        display: flex;
        gap: 0.5rem;
        justify-content: flex-end;
      }
      .btn-cancel {
        background: #e5e7eb;
        color: #1f2937;
      }
      .btn-cancel:hover {
        background: #d1d5db;
      }
      .btn-confirm {
        background: #ef4444;
      }
      .btn-confirm:hover {
        background: #dc2626;
      }
    `,Ue([he({type:String})],Fe.prototype,"label",2),Ue([he({type:String})],Fe.prototype,"link",2),Ue([he({type:String})],Fe.prototype,"message",2),Ue([ce()],Fe.prototype,"dialogOpened",1),Fe=Ue([le("as-confirm")],Fe);var Re,He,Te,Ie=Object.defineProperty,qe=Object.getOwnPropertyDescriptor,Le=e=>{throw TypeError(e)},We=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?qe(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&Ie(t,o,s),s},Ve=(e,t,o)=>t.has(e)||Le("Cannot "+o),Ke=(e,t,o)=>(Ve(e,t,"read from private field"),o?o.call(e):t.get(e)),Je=(e,t,o)=>t.has(e)?Le("Cannot add the same private member more than once"):t instanceof WeakSet?t.add(e):t.set(e,o),Ye=(e,t,o,r)=>(Ve(e,t,"write to private field"),t.set(e,o),o);let Xe=class extends se{constructor(){super(...arguments),this.label="",this.value="",this.placeholder=this.label,this.theme="",this.readonly=!1,this.disabled=!1,Je(this,Re,!1),Je(this,He,/* @__PURE__ */(new Date).getFullYear()),Je(this,Te,/* @__PURE__ */(new Date).getMonth())}get _open(){return Ke(this,Re)}set _open(e){Ye(this,Re,e)}get _year(){return Ke(this,He)}set _year(e){Ye(this,He,e)}get _month(){return Ke(this,Te)}set _month(e){Ye(this,Te,e)}render(){const e=this.value||this.placeholder||"YYYY-MM-DD";return T`
        <div class="field">
            ${this.label?T`<label>${this.label}</label>`:""}
            <div
              class="date-trigger"
              tabindex="0"
              ?aria-disabled=${this.disabled}
              @click="${()=>{this.disabled||this.readonly||(this._open=!this._open)}}"
              @blur="${()=>setTimeout(()=>this._open=!1,200)}"
            >
                <span>${e}</span>
                <span class="icon">
                    <svg viewBox="0 0 24 24">
                        <path d="M19 3h-1V1h-2v2H8V1H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11z"/>
                    </svg>
                </span>
            </div>
            ${this._open?T`
                <div class="dropdown" @mousedown="${e=>e.preventDefault()}">
                    <div class="header">
                    <button @click="${()=>{this.readonly||this.disabled||this._changeMonth(-1)}}">‹</button>
                    <div class="month-year">${this._getMonthName()} ${this._year}</div>
                    <button @click="${()=>{this.readonly||this.disabled||this._changeMonth(1)}}">›</button>
                  </div>
                    <div class="weekdays">
                        ${["S","M","T","W","T","F","S"].map(e=>T`<div class="weekday">${e}</div>`)}
                    </div>
                    <div class="days">
                        ${this._getDays().map(e=>T`
                            <div
                              class="day ${e.selected?"selected":""} ${e.otherMonth?"other-month":""}"
                              @click="${()=>{this.readonly||this.disabled||this._selectDay(e.date)}}"
                            >
                                ${e.day}
                            </div>
                        `)}
                    </div>
                </div>
            `:""}
        </div>`}_getMonthName(){return["January","February","March","April","May","June","July","August","September","October","November","December"][this._month]}_changeMonth(e){this._month+=e,this._month<0&&(this._month=11,this._year--),this._month>11&&(this._month=0,this._year++)}_getDays(){const e=new Date(this._year,this._month,1).getDay(),t=new Date(this._year,this._month+1,0).getDate(),o=new Date(this._year,this._month,0).getDate(),r=[];for(let s=e-1;s>=0;s--)r.push({day:o-s,otherMonth:!0,date:null});for(let s=1;s<=t;s++){const e=`${this._year}-${String(this._month+1).padStart(2,"0")}-${String(s).padStart(2,"0")}`;r.push({day:s,otherMonth:!1,selected:this.value===e,date:e})}const i=42-r.length;for(let s=1;s<=i;s++)r.push({day:s,otherMonth:!0,date:null});return r}_selectDay(e){e&&(this.value=e,this._open=!1,this.dispatchEvent(new CustomEvent("value-changed",{detail:{value:this.value},bubbles:!0,composed:!0})))}};Re=/* @__PURE__ */new WeakMap,He=/* @__PURE__ */new WeakMap,Te=/* @__PURE__ */new WeakMap,Xe.styles=s`
      :host {
        display: block;
        font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif;
        position: relative;
      }
      .field {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
      }
      label {
        font-size: 0.875rem;
        font-weight: 500;
        color: var(--label-color, #374151);
      }
      .date-trigger {
        padding: 0.5rem 0.75rem;
        border: 1px solid transparent;
        border-radius: 4px;
        font-size: 0.9375rem;
        font-family: inherit;
        background: var(--input-bg, #e8eaed);
        color: var(--text-color, #1a1a1a);
        outline: none;
        cursor: pointer;
        transition: border-color 0.15s;
        display: flex;
        justify-content: space-between;
        align-items: center;
        user-select: none;
      }
      .date-trigger:focus {
        border-color: var(--focus-color, #1676f3);
      }
      .icon {
        margin-left: 0.5rem;
        width: 20px;
        height: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      svg {
        width: 18px;
        height: 18px;
        fill: var(--text-color, #1f2937);
      }
      .dropdown {
        position: absolute;
        top: 100%;
        left: 0;
        background: var(--dropdown-bg, white);
        border: 1px solid var(--border-color, #d1d5db);
        border-radius: 4px;
        margin-top: 0.25rem;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        z-index: 10;
        padding: 0.5rem;
        min-width: 280px;
      }
      .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 0.5rem;
        padding: 0.25rem;
      }
      .header button {
        background: none;
        border: none;
        cursor: pointer;
        padding: 0.25rem 0.5rem;
        color: var(--text-color, #1f2937);
        font-size: 1.125rem;
        border-radius: 4px;
      }
      .header button:hover {
        background: var(--option-hover, #f3f4f6);
      }
      .month-year {
        font-weight: 500;
        font-size: 0.9375rem;
      }
      .weekdays {
        display: grid;
        grid-template-columns: repeat(7, 1fr);
        gap: 2px;
        margin-bottom: 2px;
      }
      .weekday {
        text-align: center;
        font-size: 0.75rem;
        font-weight: 500;
        padding: 0.25rem;
        color: var(--text-color, #1f2937);
        opacity: 0.6;
      }
      .days {
        display: grid;
        grid-template-columns: repeat(7, 1fr);
        gap: 2px;
      }
      .day {
        text-align: center;
        padding: 0.5rem;
        cursor: pointer;
        border-radius: 4px;
        font-size: 0.875rem;
        color: var(--text-color, #1f2937);
      }
      .day:hover {
        background: var(--option-hover, #f3f4f6);
      }
      .day.selected {
        background: var(--option-selected, #1676f3);
        color: white;
      }
      .day.other-month {
        opacity: 0.3;
      }
      :host([theme="dark"]) {
        --label-color: #f3f4f6;
        --border-color: #525252;
        --input-bg: #374151;
        --text-color: #e5e5e5;
        --focus-color: #1676f3;
        --dropdown-bg: #262626;
        --option-hover: #404040;
        --option-selected: #1676f3;
      }
    `,We([he({type:String})],Xe.prototype,"label",2),We([he({type:String})],Xe.prototype,"value",2),We([he({type:String})],Xe.prototype,"placeholder",2),We([he({type:String})],Xe.prototype,"theme",2),We([he({type:Boolean,reflect:!0})],Xe.prototype,"readonly",2),We([he({type:Boolean,reflect:!0})],Xe.prototype,"disabled",2),We([ce()],Xe.prototype,"_open",1),We([ce()],Xe.prototype,"_year",1),We([ce()],Xe.prototype,"_month",1),Xe=We([le("as-date")],Xe);var Ze,Ge,Qe,et,tt=Object.defineProperty,ot=Object.getOwnPropertyDescriptor,rt=e=>{throw TypeError(e)},it=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?ot(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&tt(t,o,s),s},st=(e,t,o)=>t.has(e)||rt("Cannot "+o),at=(e,t,o)=>(st(e,t,"read from private field"),o?o.call(e):t.get(e)),lt=(e,t,o)=>t.has(e)?rt("Cannot add the same private member more than once"):t instanceof WeakSet?t.add(e):t.set(e,o),nt=(e,t,o,r)=>(st(e,t,"write to private field"),t.set(e,o),o);let dt=class extends se{constructor(){super(...arguments),this.label="",this.value="",this.placeholder=this.label,this.theme="",this.readonly=!1,this.disabled=!1,lt(this,Ze,!1),lt(this,Ge,"12"),lt(this,Qe,"00"),lt(this,et,"AM")}get _open(){return at(this,Ze)}set _open(e){nt(this,Ze,e)}get _hour(){return at(this,Ge)}set _hour(e){nt(this,Ge,e)}get _minute(){return at(this,Qe)}set _minute(e){nt(this,Qe,e)}get _period(){return at(this,et)}set _period(e){nt(this,et,e)}render(){const e=this.value||this.placeholder||"HH:MM";return T`
        <div class="field">
            ${this.label?T`<label>${this.label}</label>`:""}
            <div
              class="time-trigger"
              tabindex="0"
              ?aria-disabled=${this.disabled}
              @click="${()=>{this.disabled||this.readonly||(this._open=!this._open)}}"
              @blur="${()=>setTimeout(()=>this._open=!1,200)}"
            >
                <span>${e}</span>
                <span class="icon">
                    <svg viewBox="0 0 24 24">
                        <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm.5-13H11v6l5.2 3.2.8-1.3-4.5-2.7V7z"/>
                    </svg>
                </span>
            </div>
            ${this._open?T`
                <div class="dropdown">
                    <div class="time-display">${this._hour}:${this._minute} ${this._period}</div>
                    <div class="selectors">
                        <div class="column">
                            ${Array.from({length:12},(e,t)=>{const o=(t+1).toString().padStart(2,"0");return T`
                                    <div
                                      class="option ${this._hour===o?"selected":""}"
                                      @click="${()=>{this.readonly||this.disabled||(this._hour=o,this._updateValue())}}"
                                    >
                                        ${o}
                                    </div>
                                `})}
                        </div>
                        <div class="column">
                            ${Array.from({length:12},(e,t)=>{const o=(5*t).toString().padStart(2,"0");return T`
                                    <div
                                      class="option ${this._minute===o?"selected":""}"
                                      @click="${()=>{this.readonly||this.disabled||(this._minute=o,this._updateValue())}}"
                                    >
                                        ${o}
                                    </div>
                                `})}
                        </div>
                        <div class="period-column">
                            <div
                                class="option ${"AM"===this._period?"selected":""}"
                                @click="${()=>{this.readonly||this.disabled||(this._period="AM",this._updateValue())}}"
                            >
                                AM
                            </div>
                            <div
                                class="option ${"PM"===this._period?"selected":""}"
                                @click="${()=>{this.readonly||this.disabled||(this._period="PM",this._updateValue())}}"
                            >
                                PM
                            </div>
                        </div>
                    </div>
                </div>
            `:""}
        </div>`}_updateValue(){let e=parseInt(this._hour);"PM"===this._period&&12!==e&&(e+=12),"AM"===this._period&&12===e&&(e=0),this.value=`${e.toString().padStart(2,"0")}:${this._minute}`,this.dispatchEvent(new CustomEvent("value-changed",{detail:{value:this.value},bubbles:!0,composed:!0}))}};Ze=/* @__PURE__ */new WeakMap,Ge=/* @__PURE__ */new WeakMap,Qe=/* @__PURE__ */new WeakMap,et=/* @__PURE__ */new WeakMap,dt.styles=s`
      :host {
        display: block;
        font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif;
        position: relative;
      }
      .field {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
      }
      label {
        font-size: 0.875rem;
        font-weight: 500;
        color: var(--label-color, #374151);
      }
      .time-trigger {
        padding: 0.5rem 0.75rem;
        border: 1px solid transparent;
        border-radius: 4px;
        font-size: 0.9375rem;
        font-family: inherit;
        background: var(--input-bg, #e8eaed);
        color: var(--text-color, #1a1a1a);
        outline: none;
        cursor: pointer;
        transition: border-color 0.15s;
        display: flex;
        justify-content: space-between;
        align-items: center;
        user-select: none;
      }
      .time-trigger:focus {
        border-color: var(--focus-color, #1676f3);
      }
      .icon {
        margin-left: 0.5rem;
        width: 20px;
        height: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      svg {
        width: 18px;
        height: 18px;
        fill: var(--text-color, #1f2937);
      }
      .dropdown {
        position: absolute;
        top: 100%;
        left: 0;
        background: var(--dropdown-bg, white);
        border: 1px solid var(--border-color, #d1d5db);
        border-radius: 4px;
        margin-top: 0.25rem;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        z-index: 10;
        padding: 0;
        width: 280px;
      }
      .time-display {
        text-align: center;
        font-size: 2.5rem;
        font-weight: 300;
        padding: 1.5rem 1rem 1rem 1rem;
        color: var(--text-color, #1f2937);
        border-bottom: 1px solid var(--border-color, #d1d5db);
      }
      .selectors {
        display: flex;
        height: 240px;
      }
      .column {
        flex: 1;
        overflow-y: auto;
        overflow-x: hidden;
        border-right: 1px solid var(--border-color, #d1d5db);
      }
      .column:last-child {
        border-right: none;
      }
      .period-column {
        flex: 0 0 60px;
        display: flex;
        flex-direction: column;
      }
      .option {
        padding: 0.75rem;
        cursor: pointer;
        font-size: 0.9375rem;
        color: var(--text-color, #1f2937);
        text-align: center;
        border-bottom: 1px solid transparent;
      }
      .option:hover {
        background: var(--option-hover, #f3f4f6);
      }
      .option.selected {
        background: var(--option-selected, #e3f2fd);
        color: var(--selected-text, #1676f3);
        font-weight: 600;
      }
      .period-column .option {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        border-bottom: none;
      }
      .period-column .option.selected {
        background: var(--option-selected, #1676f3);
        color: white;
      }
      :host([theme="dark"]) {
        --label-color: #f3f4f6;
        --border-color: #525252;
        --input-bg: #374151;
        --text-color: #e5e5e5;
        --focus-color: #1676f3;
        --dropdown-bg: #262626;
        --option-hover: #404040;
        --option-selected: #1e3a5f;
      }
    `,it([he({type:String})],dt.prototype,"label",2),it([he({type:String})],dt.prototype,"value",2),it([he({type:String})],dt.prototype,"placeholder",2),it([he({type:String})],dt.prototype,"theme",2),it([he({type:Boolean,reflect:!0})],dt.prototype,"readonly",2),it([he({type:Boolean,reflect:!0})],dt.prototype,"disabled",2),it([ce()],dt.prototype,"_open",1),it([ce()],dt.prototype,"_hour",1),it([ce()],dt.prototype,"_minute",1),it([ce()],dt.prototype,"_period",1),dt=it([le("as-time")],dt);var ht=Object.defineProperty,ct=Object.getOwnPropertyDescriptor,pt=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?ct(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&ht(t,o,s),s};let ut=class extends se{constructor(){super(),this.width=1200,this.height=675,this.url="",this.width=1200,this.height=675,this.url=""}render(){return T`
        <div class="embed-container">
            <iframe
            width="${this.width}"
            height="${this.height}"
            frameborder="0"
            src="${this.url}"
            type="text/html"
            allowScriptAccess="always"
            allowFullScreen
            scrolling="yes"
            allowNetworking="all"
            ></iframe>
        </div>`}};ut.styles=s`
    :host {
        width: 100%;
    }

    .embed-container {
        position: relative;
        padding-bottom: 56.25%;
        padding-top: 0;
        height: 0;
    }

    iframe {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
    }`,ut.properties={width:{type:Number},height:{type:Number},url:{type:String}},pt([he({type:String})],ut.prototype,"width",2),pt([he({type:String})],ut.prototype,"height",2),pt([he({type:String})],ut.prototype,"url",2),ut=pt([le("as-embed")],ut);var mt=Object.defineProperty,bt=Object.getOwnPropertyDescriptor,ft=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?bt(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&mt(t,o,s),s};let gt=class extends se{constructor(){super(...arguments),this.url=""}render(){return T`
        <div class="image-container">
          <br />
          <img src="${this.url}" />
          <br />
        </div>`}};gt.styles=s`
    :host {
        display: flex;
        justify-content: center;
    }

    img {
        margin: 10px;
    }

    .image-container {
        display: flex;
        justify-content: center;
    }`,ft([he({type:String})],gt.prototype,"url",2),gt=ft([le("as-image")],gt);class vt{planeDeserialize(e){const t=e.split(";"),o=[];try{""!==e&&"[]"!==e&&t.forEach(e=>{const t=e.split(","),r={};t.forEach(e=>{const[t,o]=e.split("=");r[t]=o}),o.push(r)})}catch(r){console.log("planeDeserialize => IndexOutOfBounds! input =",e)}return o}}var yt=Object.defineProperty,$t=Object.getOwnPropertyDescriptor,_t=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?$t(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&yt(t,o,s),s};let xt=class extends se{constructor(){super(...arguments),this.label="",this.value="",this.options="label=A,value=A;label=B,value=B;label=C,value=C",this.theme="",this.readonly=!1,this.disabled=!1}get items(){return(new vt).planeDeserialize(this.options).map(e=>({label:e.label,value:e.value}))}render(){return T`
        <div class="group">
            ${this.label?T`<div class="group-label">${this.label}</div>`:""}
            <div class="options">
                ${this.items.map(e=>T`
                    <label class="option">
                        <input
                          type="radio"
                          name="radio-group"
                          .value="${e.value}"
                          ?checked="${this.value===e.value}"
                          ?disabled=${this.disabled}
                          ?readonly=${this.readonly}
                          @change="${t=>{this.readonly?t.target.checked=this.value===e.value:(this.value=t.target.value,this.dispatchEvent(new CustomEvent("value-changed",{detail:{value:this.value},bubbles:!0,composed:!0})))}}"
                        />
                        ${e.label}
                    </label>
                `)}
            </div>
        </div>`}};xt.styles=s`
      :host {
        display: block;
        font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif;
      }
      .group {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
      }
      .group-label {
        font-size: 0.875rem;
        font-weight: 500;
        color: var(--label-color, #374151);
        margin-bottom: 0.25rem;
      }
      .options {
        display: flex;
        gap: 1rem;
      }
      .option {
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
        cursor: pointer;
        font-size: 0.9375rem;
        color: var(--text-color, #1f2937);
      }
      input[type="radio"] {
        width: 16px;
        height: 16px;
        cursor: pointer;
        appearance: none;
        -webkit-appearance: none;
        border: 2px solid var(--radio-border, #d0d7de);
        border-radius: 50%;
        background: var(--radio-bg, #fafafa);
        position: relative;
      }
      input[type="radio"]:checked {
        border-color: var(--accent-color, #1676f3);
      }
      input[type="radio"]:checked::after {
        content: '';
        position: absolute;
        left: 2px;
        top: 2px;
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: var(--accent-color, #1676f3);
      }
      :host([theme="dark"]) {
        --label-color: #f3f4f6;
        --text-color: #e5e5e5;
        --accent-color: #1676f3;
      }
    `,_t([he({type:String})],xt.prototype,"label",2),_t([he({type:String})],xt.prototype,"value",2),_t([he({type:String})],xt.prototype,"options",2),_t([he({type:String})],xt.prototype,"theme",2),_t([he({type:Array})],xt.prototype,"items",1),_t([he({type:Boolean,reflect:!0})],xt.prototype,"readonly",2),_t([he({type:Boolean,reflect:!0})],xt.prototype,"disabled",2),xt=_t([le("as-radio")],xt);var wt,kt=Object.defineProperty,St=Object.getOwnPropertyDescriptor,At=e=>{throw TypeError(e)},Ct=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?St(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&kt(t,o,s),s},Et=(e,t,o)=>t.has(e)||At("Cannot "+o);let Ot=class extends se{constructor(){var e,t,o;super(...arguments),this.label="",this.value="",this.options="label=A,value=A;label=B,value=B;label=C,value=C",this.theme="",this.readonly=!1,this.disabled=!1,e=this,o=!1,(t=wt).has(e)?At("Cannot add the same private member more than once"):t instanceof WeakSet?t.add(e):t.set(e,o)}get _open(){return Et(e=this,t=wt,"read from private field"),o?o.call(e):t.get(e);var e,t,o}set _open(e){var t,o,r;r=e,Et(t=this,o=wt,"write to private field"),o.set(t,r)}get items(){return(new vt).planeDeserialize(this.options).map(e=>({label:e.label,value:e.value}))}render(){const e=this.items.find(e=>e.value===this.value)||this.items[0];return T`
        <div class="field">
            ${this.label?T`<label>${this.label}</label>`:""}
            <div
              class="select-trigger"
              tabindex="0"
              ?aria-disabled=${this.disabled}
              @click="${()=>{this.disabled||this.readonly||(this._open=!this._open)}}"
              @blur="${()=>setTimeout(()=>this._open=!1,200)}"
            >
                <span>${e?.label||""}</span>
                <span class="arrow ${this._open?"open":""}">
                    <svg viewBox="0 0 24 24">
                        <path d="M7 10l5 5 5-5" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </span>
            </div>
            ${this._open?T`
                <div class="dropdown">
                    ${this.items.map(e=>T`
                        <div
                            class="option ${this.value===e.value?"selected":""}"
                          @click="${()=>{this.readonly||(this.value=e.value,this._open=!1,this.dispatchEvent(new CustomEvent("value-changed",{detail:{value:this.value},bubbles:!0,composed:!0})))}}"
                        >
                            ${e.label}
                        </div>
                    `)}
                </div>
            `:""}
        </div>
        `}};wt=/* @__PURE__ */new WeakMap,Ot.styles=s`
      :host {
        display: block;
        font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif;
        position: relative;
      }
      .field {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
      }
      label {
        font-size: 0.875rem;
        font-weight: 500;
        color: var(--label-color, #374151);
      }
      .select-trigger {
        padding: 0.5rem 0.75rem;
        border: 1px solid transparent;
        border-radius: 4px;
        font-size: 0.9375rem;
        font-family: inherit;
        background: var(--input-bg, #e8eaed);
        color: var(--text-color, #1a1a1a);
        outline: none;
        cursor: pointer;
        transition: border-color 0.15s;
        display: flex;
        justify-content: space-between;
        align-items: center;
        user-select: none;
      }
      .select-trigger:focus {
        border-color: var(--focus-color, #1676f3);
      }
      .arrow {
        margin-left: 0.5rem;
        width: 20px;
        height: 20px;
        transition: transform 0.2s;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      .arrow.open {
        transform: rotate(180deg);
      }
      svg {
        width: 20px;
        height: 20px;
        color: var(--text-color, #1f2937);
      }
      .dropdown {
        position: absolute;
        top: 100%;
        left: 0;
        right: 0;
        max-height: 200px;
        overflow-y: auto;
        background: var(--dropdown-bg, white);
        border: 1px solid var(--border-color, #d1d5db);
        border-radius: 4px;
        margin-top: 0.25rem;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        z-index: 10;
      }
      .option {
        padding: 0.5rem 0.75rem;
        cursor: pointer;
        font-size: 0.9375rem;
        color: var(--text-color, #1f2937);
      }
      .option:hover {
        background: var(--option-hover, #f3f4f6);
      }
      .option.selected {
        background: var(--option-selected, #e0f2fe);
      }
      :host([theme="dark"]) {
        --label-color: #f3f4f6;
        --border-color: #525252;
        --input-bg: #374151;
        --text-color: #e5e5e5;
        --focus-color: #1676f3;
        --dropdown-bg: #262626;
        --option-hover: #404040;
        --option-selected: #1e3a5f;
      }
    `,Ct([he({type:String})],Ot.prototype,"label",2),Ct([he({type:String})],Ot.prototype,"value",2),Ct([he({type:String})],Ot.prototype,"options",2),Ct([he({type:String})],Ot.prototype,"theme",2),Ct([he({type:Boolean,reflect:!0})],Ot.prototype,"readonly",2),Ct([he({type:Boolean,reflect:!0})],Ot.prototype,"disabled",2),Ct([ce()],Ot.prototype,"_open",1),Ct([he({type:Array})],Ot.prototype,"items",1),Ot=Ct([le("as-select")],Ot);var Mt,Dt,Pt=Object.defineProperty,zt=Object.getOwnPropertyDescriptor,jt=e=>{throw TypeError(e)},Bt=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?zt(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&Pt(t,o,s),s},Ut=(e,t,o)=>t.has(e)||jt("Cannot "+o),Nt=(e,t,o)=>(Ut(e,t,"read from private field"),o?o.call(e):t.get(e)),Ft=(e,t,o)=>t.has(e)?jt("Cannot add the same private member more than once"):t instanceof WeakSet?t.add(e):t.set(e,o),Rt=(e,t,o,r)=>(Ut(e,t,"write to private field"),t.set(e,o),o);let Ht=class extends se{constructor(){super(...arguments),this.label="",this.value="",this.options="label=A,value=A;label=B,value=B;label=C,value=C",this.theme="",this.readonly=!1,this.disabled=!1,Ft(this,Mt,""),Ft(this,Dt,!1)}get _filter(){return Nt(this,Mt)}set _filter(e){Rt(this,Mt,e)}get _open(){return Nt(this,Dt)}set _open(e){Rt(this,Dt,e)}get items(){return(new vt).planeDeserialize(this.options).map(e=>({label:e.label,value:e.value}))}render(){const e=this._filter?this.items.filter(e=>e.label.toLowerCase().includes(this._filter.toLowerCase())):this.items;return T`
        <div class="field">
            ${this.label?T`<label>${this.label}</label>`:""}
            <input
                type="text"
                .value="${this._filter}"
                placeholder="${this.label||"Buscar..."}"
              ?readonly=${this.readonly}
              ?disabled=${this.disabled}
              @input="${e=>{this._filter=e.target.value,this._open=!0}}"
                @focus="${()=>this._open=!0}"
                @blur="${()=>setTimeout(()=>this._open=!1,200)}"
            />
            ${this._open&&e.length?T`
                <div class="dropdown">
                    ${e.map(e=>T`
                        <div class="option" @click="${()=>{this.value=e.value,this._filter=e.label,this._open=!1,this.dispatchEvent(new CustomEvent("value-changed",{detail:{value:this.value},bubbles:!0,composed:!0}))}}">
                            ${e.label}
                        </div>
                    `)}
                </div>
            `:""}
        </div>
        `}};Mt=/* @__PURE__ */new WeakMap,Dt=/* @__PURE__ */new WeakMap,Ht.styles=s`
      :host {
        display: block;
        font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif;
        position: relative;
      }
      .field {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
      }
      label {
        font-size: 0.875rem;
        font-weight: 500;
        color: var(--label-color, #374151);
      }
      input {
        padding: 0.5rem 0.75rem;
        border: 1px solid transparent;
        border-radius: 4px;
        font-size: 0.9375rem;
        font-family: inherit;
        background: var(--input-bg, #e8eaed);
        color: var(--text-color, #1a1a1a);
        outline: none;
        transition: border-color 0.15s;
      }
      input:focus {
        border-color: var(--focus-color, #1676f3);
      }
      .dropdown {
        position: absolute;
        top: 100%;
        left: 0;
        right: 0;
        max-height: 200px;
        overflow-y: auto;
        background: var(--dropdown-bg, white);
        border: 1px solid var(--border-color, #d1d5db);
        border-radius: 4px;
        margin-top: 0.25rem;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        z-index: 10;
      }
      .option {
        padding: 0.5rem 0.75rem;
        cursor: pointer;
        font-size: 0.9375rem;
        color: var(--text-color, #1f2937);
      }
      .option:hover {
        background: var(--option-hover, #f3f4f6);
      }
      :host([theme="dark"]) {
        --label-color: #f3f4f6;
        --border-color: #525252;
        --input-bg: #374151;
        --text-color: #e5e5e5;
        --focus-color: #1676f3;
        --dropdown-bg: #262626;
        --option-hover: #404040;
      }
    `,Bt([he({type:String})],Ht.prototype,"label",2),Bt([he({type:String})],Ht.prototype,"value",2),Bt([he({type:String})],Ht.prototype,"options",2),Bt([he({type:String})],Ht.prototype,"theme",2),Bt([he({type:Boolean,reflect:!0})],Ht.prototype,"readonly",2),Bt([he({type:Boolean,reflect:!0})],Ht.prototype,"disabled",2),Bt([ce()],Ht.prototype,"_filter",1),Bt([ce()],Ht.prototype,"_open",1),Bt([he({type:Array})],Ht.prototype,"items",1),Ht=Bt([le("as-complete")],Ht);var Tt=Object.defineProperty,It=Object.getOwnPropertyDescriptor,qt=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?It(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&Tt(t,o,s),s};let Lt=class extends se{constructor(){super(...arguments),this.label="",this.value="",this.placeholder=this.label,this.rows=3,this.theme="",this.readonly=!1,this.disabled=!1}render(){return T`
        <div class="field">
            ${this.label?T`<label>${this.label}</label>`:""}
            <textarea
              rows="${this.rows}"
              placeholder="${this.placeholder}"
              .value="${this.value}"
              ?readonly=${this.readonly}
              ?disabled=${this.disabled}
              @input="${e=>{this.readonly||(this.value=e.target.value,this.dispatchEvent(new CustomEvent("value-changed",{detail:{value:this.value},bubbles:!0,composed:!0})))}}"
            ></textarea>
        </div>
        `}};Lt.styles=s`
      :host {
        display: block;
        font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif;
      }
      .field {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
      }
      label {
        font-size: 0.875rem;
        font-weight: 500;
        color: var(--label-color, #374151);
      }
      textarea {
        padding: 0.5rem 0.75rem;
        border: 1px solid transparent;
        border-radius: 4px;
        font-size: 0.9375rem;
        font-family: inherit;
        background: var(--input-bg, #e8eaed);
        color: var(--text-color, #1a1a1a);
        outline: none;
        resize: vertical;
        transition: border-color 0.15s;
      }
      textarea:focus {
        border-color: var(--focus-color, #1676f3);
      }
      textarea::placeholder {
        color: var(--placeholder-color, #737373);
      }
      :host([theme="dark"]) {
        --label-color: #f3f4f6;
        --border-color: #525252;
        --input-bg: #374151;
        --text-color: #e5e5e5;
        --placeholder-color: #737373;
        --focus-color: #1676f3;
      }
    `,qt([he({type:String})],Lt.prototype,"label",2),qt([he({type:String})],Lt.prototype,"value",2),qt([he({type:String})],Lt.prototype,"placeholder",2),qt([he({type:Number})],Lt.prototype,"rows",2),qt([he({type:String})],Lt.prototype,"theme",2),qt([he({type:Boolean,reflect:!0})],Lt.prototype,"readonly",2),qt([he({type:Boolean,reflect:!0})],Lt.prototype,"disabled",2),Lt=qt([le("as-text")],Lt);var Wt,Vt=Object.defineProperty,Kt=Object.getOwnPropertyDescriptor,Jt=e=>{throw TypeError(e)},Yt=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?Kt(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&Vt(t,o,s),s},Xt=(e,t,o)=>t.has(e)||Jt("Cannot "+o);let Zt=class extends se{constructor(){var e,t,o;super(...arguments),this.label="Upload files",this.accept="*",this.multiple=!1,this.theme="",this.disabled=!1,e=this,o=!1,(t=Wt).has(e)?Jt("Cannot add the same private member more than once"):t instanceof WeakSet?t.add(e):t.set(e,o)}get _dragOver(){return Xt(e=this,t=Wt,"read from private field"),o?o.call(e):t.get(e);var e,t,o}set _dragOver(e){var t,o,r;r=e,Xt(t=this,o=Wt,"write to private field"),o.set(t,r)}_handleClick(){this.disabled||this.shadowRoot?.querySelector("input")?.click()}_handleFileChange(e){const t=e.target.files;t&&this.dispatchEvent(new CustomEvent("files-selected",{detail:{files:Array.from(t)},bubbles:!0,composed:!0}))}_handleDragOver(e){this.disabled||(e.preventDefault(),this._dragOver=!0)}_handleDragLeave(){this._dragOver=!1}_handleDrop(e){if(this.disabled)return;e.preventDefault(),this._dragOver=!1;const t=e.dataTransfer?.files;t&&this.dispatchEvent(new CustomEvent("files-selected",{detail:{files:Array.from(t)},bubbles:!0,composed:!0}))}render(){return T`
            <div 
                class="upload-area ${this._dragOver?"drag-over":""} ${this.disabled?"disabled":""}"
                @click="${this._handleClick}"
                @dragover="${this._handleDragOver}"
                @dragleave="${this._handleDragLeave}"
                @drop="${this._handleDrop}"
            >
                <div class="upload-icon">📁</div>
                <div class="upload-text">${this.label}</div>
                <div class="upload-hint">Click or drag files here</div>
                <input 
                    type="file" 
                    .accept="${this.accept}"
                    ?multiple="${this.multiple}"
                    @change="${this._handleFileChange}"
                />
            </div>
        `}};Wt=/* @__PURE__ */new WeakMap,Zt.styles=s`
        :host {
            display: block;
            font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif;
        }
        .upload-area {
            border: 2px dashed var(--border-color, #d1d5db);
            border-radius: 8px;
            padding: 2rem;
            text-align: center;
            background: var(--bg-color, #f9fafb);
            color: var(--text-color, #374151);
            cursor: pointer;
            transition: all 0.15s;
        }
        .upload-area:hover:not(.disabled) {
            border-color: var(--hover-border, #3b82f6);
            background: var(--hover-bg, #eff6ff);
        }
        .upload-area.drag-over {
            border-color: var(--focus-color, #3b82f6);
            background: var(--focus-bg, #dbeafe);
        }
        .upload-area.disabled {
            opacity: 0.5;
            cursor: not-allowed;
        }
        .upload-icon {
            font-size: 2rem;
            margin-bottom: 0.5rem;
        }
        .upload-text {
            font-size: 0.9375rem;
            margin-bottom: 0.25rem;
        }
        .upload-hint {
            font-size: 0.875rem;
            opacity: 0.7;
        }
        input[type="file"] {
            display: none;
        }
        :host([theme="dark"]) {
            --border-color: #4b5563;
            --bg-color: #1f2937;
            --text-color: #f3f4f6;
            --hover-border: #3b82f6;
            --hover-bg: #1e3a5f;
            --focus-color: #3b82f6;
            --focus-bg: #1d4ed8;
        }
    `,Yt([he({type:String})],Zt.prototype,"label",2),Yt([he({type:String})],Zt.prototype,"accept",2),Yt([he({type:Boolean})],Zt.prototype,"multiple",2),Yt([he({type:String})],Zt.prototype,"theme",2),Yt([he({type:Boolean})],Zt.prototype,"disabled",2),Yt([ce()],Zt.prototype,"_dragOver",1),Zt=Yt([le("as-upload")],Zt);var Gt=Object.defineProperty,Qt=Object.getOwnPropertyDescriptor,eo=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?Qt(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&Gt(t,o,s),s};let to=class extends se{constructor(){super(),this.width=560,this.height=315,this.url="",this.width=560,this.height=315,this.url=""}updated(e){e.has("width")&&window.innerWidth<560&&(this.width=310,this.height=175)}render(){return T`
        <div class="video">
            <br />
            <iframe
                width="${this.width}"
                height="${this.height}"
                frameborder="0"
                src="${this.url}"
                allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture"
                allowfullscreen
            ></iframe>
            <br />
        </div>`}};to.styles=s`
    .video {
      display: grid;
      grid-template-areas: stack;
      place-items: center;
      width: max(320px, 100%);
    }`,eo([he({type:String})],to.prototype,"width",2),eo([he({type:String})],to.prototype,"height",2),eo([he({type:String})],to.prototype,"url",2),to=eo([le("as-video")],to);var oo,ro,io,so,ao,lo=Object.defineProperty,no=Object.getOwnPropertyDescriptor,ho=e=>{throw TypeError(e)},co=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?no(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&lo(t,o,s),s},po=(e,t,o)=>t.has(e)||ho("Cannot "+o),uo=(e,t,o)=>(po(e,t,"read from private field"),o?o.call(e):t.get(e)),mo=(e,t,o)=>t.has(e)?ho("Cannot add the same private member more than once"):t instanceof WeakSet?t.add(e):t.set(e,o),bo=(e,t,o,r)=>(po(e,t,"write to private field"),t.set(e,o),o);let fo=class extends se{constructor(){super(...arguments),this.data=[],this.columns=[],this.pageSize=15,this.title="",this.theme="",this.selectable=!1,this.pageable=!1,this.filterable=!1,this.actionable=!1,mo(this,oo,""),mo(this,ro,null),mo(this,io,1),mo(this,so,0),mo(this,ao,null)}get _filter(){return uo(this,oo)}set _filter(e){bo(this,oo,e)}get _sortKey(){return uo(this,ro)}set _sortKey(e){bo(this,ro,e)}get _sortDir(){return uo(this,io)}set _sortDir(e){bo(this,io,e)}get _page(){return uo(this,so)}set _page(e){bo(this,so,e)}get _selectedRow(){return uo(this,ao)}set _selectedRow(e){bo(this,ao,e)}_getFilteredData(){if(!this._filter)return this.data;const e=this._filter.toLowerCase();return this.data.filter(t=>Object.values(t).some(t=>String(t).toLowerCase().includes(e)))}_getSortedData(){const e=this._getFilteredData();return this._sortKey?[...e].sort((e,t)=>{const o=e[this._sortKey],r=t[this._sortKey];return o<r?-this._sortDir:o>r?this._sortDir:0}):e}_getPaginatedData(){const e=this._getSortedData();if(!this.pageable)return e;const t=this._page*this.pageSize;return e.slice(t,t+this.pageSize)}_sort(e){this._sortKey===e?this._sortDir=1===this._sortDir?-1:1:(this._sortKey=e,this._sortDir=1)}_selectRow(e){this.selectable&&(this._selectedRow=e,this.dispatchEvent(new CustomEvent("row-select",{detail:{row:e,id:e.id},bubbles:!0,composed:!0})))}render(){if(!this.data.length||!this.columns.length)return T``;const e=this._getPaginatedData(),t=this._getSortedData().length,o=Math.ceil(t/this.pageSize);return T`
      <div class="container">
        ${this.title||this.filterable?T`
          <div class="header">
            ${this.title?T`<div class="title">${this.title}</div>`:T`<div></div>`}
            ${this.filterable?T`
              <input
                type="text"
                class="filter-input"
                placeholder="🔍"
                .value=${this._filter}
                @input=${e=>{this._filter=e.target.value,this._page=0}}
              />
            `:""}
          </div>
        `:""}

        <div class="table-wrapper">
          <table>
          <thead>
            <tr>
              ${this.columns.map(e=>T`
                <th @click=${()=>this._sort(e.key)}>
                  ${e.header}
                  ${this._sortKey===e.key?1===this._sortDir?" ↑":" ↓":""}
                </th>
              `)}
              ${this.actionable?T`<th class="action-col"></th>`:""}
            </tr>
          </thead>
          <tbody>
            ${e.map(e=>T`
              <tr 
                class="${this.selectable?"selectable":""} ${this._selectedRow===e?"selected":""}"
                @click=${()=>this._selectRow(e)}
              >
                ${this.columns.map((t,o)=>T`<td class="${0===o?"first-col":""}">${e[t.key]}</td>`)}
                ${this.actionable?T`
                  <td class="action-col">
                    <button class="action-btn" @click=${t=>{t.stopPropagation(),this.selectable&&(this._selectedRow=e),this.dispatchEvent(new CustomEvent("row-action",{detail:{row:e,id:e.id,event:t},bubbles:!0,composed:!0}))}}>⋮</button>
                  </td>
                `:""}
              </tr>
            `)}
          </tbody>
          </table>
        </div>

        ${this.pageable?T`
          <div class="pagination">
            <div># ${t}</div>
            <div class="pagination-controls">
              <button 
                @click=${()=>this._page--}
                ?disabled=${0===this._page}
              >
                &lt;
              </button>
              <span>${this._page+1} / ${o}</span>
              <button 
                @click=${()=>this._page++}
                ?disabled=${this._page>=o-1}
              >
                &gt;
              </button>
            </div>
          </div>
        `:""}
      </div>
    `}};oo=/* @__PURE__ */new WeakMap,ro=/* @__PURE__ */new WeakMap,io=/* @__PURE__ */new WeakMap,so=/* @__PURE__ */new WeakMap,ao=/* @__PURE__ */new WeakMap,fo.styles=s`
    :host {
      display: block;
      font-family: var(--lumo-font-family, -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol");
      --table-bg: white;
      --table-text: #1f2937;
      --table-border: #e5e7eb;
      --table-border-strong: #d1d5db;
      --table-row-even: #f9fafb;
      --table-row-hover: #e0f2fe;
      --table-row-selected: #dbeafe;
      --input-bg: var(--lumo-contrast-10pct, #f5f5f5);
      --input-border: var(--lumo-contrast-20pct, #d1d5db);
      --input-text: var(--lumo-body-text-color, #1f2937);
      --button-bg: white;
      --button-hover: #f3f4f6;
    }

    :host([theme="dark"]) {
      --table-bg: #1f2937;
      --table-text: #f3f4f6;
      --table-border: #374151;
      --table-border-strong: #4b5563;
      --table-row-even: #111827;
      --table-row-hover: #1e3a5f;
      --table-row-selected: #1d4ed8;
      --input-bg: var(--lumo-contrast-10pct, #374151);
      --input-border: var(--lumo-contrast-20pct, #4b5563);
      --input-text: var(--lumo-body-text-color, #f3f4f6);
      --button-bg: #374151;
      --button-hover: #4b5563;
    }
    :host([theme="light"]) {
      --table-bg: white;
      --table-text: #1f2937;
      --table-border: #e5e7eb;
      --table-border-strong: #d1d5db;
      --table-row-even: #f9fafb;
      --table-row-hover: #e0f2fe;
      --table-row-selected: #dbeafe;
      --input-bg: var(--lumo-contrast-10pct, white);
      --input-border: var(--lumo-contrast-20pct, #d1d5db);
      --input-text: var(--lumo-body-text-color, #1f2937);
      --button-bg: white;
      --button-hover: #f3f4f6;
    }
    .container {
      background: var(--table-bg);
      border-radius: 0.5rem;
      box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
      overflow: hidden;
      color: var(--table-text);
    }
    .container > .header {
      padding: 0 0.5rem;
    }
    .container > .header:first-child {
      padding-top: 0.4rem;
    }
    .header {
      padding: 0 1.5rem 1rem 1.5rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .title {
      font-size: 1.25rem;
      font-weight: 600;
    }
    .table-wrapper {
      overflow-x: auto;
      -webkit-overflow-scrolling: touch;
    }
    table {
      width: 100%;
    }
    thead {
      background-color: var(--table-row-even);
      border-bottom: 1px solid var(--table-border);
    }
    th {
      padding: 0.5rem 0.25rem;
      text-align: left;
      font-size: 0.75rem;
      font-weight: 500;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      cursor: pointer;
      user-select: none;
      color: var(--table-text);
      opacity: 0.7;
    }

    tbody tr {
      border-bottom: 1px solid var(--table-border);
      transition: background-color 0.15s;
    }
    tbody tr:nth-child(even) {
      background-color: var(--table-row-even);
    }

    td {
      padding: 0.5rem 0.25rem;
      font-size: 0.9375rem;
      color: var(--table-text);
    }
    td.first-col {
      border-left: 3px solid transparent;
    }
    tbody tr.selected td.first-col {
      border-left-color: #1676f3;
    }
    th.action-col {
      width: 0.5rem;
      text-align: center;
      cursor: default;
      padding: 0;
    }
    td.action-col {
      width: 0.5rem;
      text-align: center;
      padding: 0;
    }
    .action-btn {
      background: transparent;
      border: none;
      color: var(--table-text);
      cursor: pointer;
      font-size: 1.25rem;
      padding: 0;
      border-radius: 4px;
      line-height: 1;
    }
    .action-btn:hover {
      background: var(--option-hover, #f3f4f6);
    }
    tbody tr:hover {
      background-color: var(--table-row-hover);
    }
    tbody tr.selectable {
      cursor: pointer;
    }
    tbody tr.selected {
      background-color: var(--table-row-selected) !important;
      height: auto;
    }
    .pagination {
      padding: 0.6rem 0.5rem 0.5rem 0.5rem;
      border-top: 1px solid var(--table-border);
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 0.875rem;
    }
    .pagination-controls {
      display: flex;
      gap: 0.5rem;
      align-items: center;
    }
    .filter-input {
      width: 160px;
      padding: 0.5rem 0.75rem;
      border: none;
      border-radius: var(--lumo-border-radius-m, 4px);
      font-size: var(--lumo-font-size-m, 0.9375rem);
      font-family: var(--lumo-font-family);
      background: var(--input-bg);
      color: var(--input-text);
      outline: none;
    }
    .filter-input:focus {
      border: 1px solid var(--lumo-primary-color, #3b82f6);
      box-shadow: 0 0 0 2px var(--lumo-primary-color-10pct, rgba(59, 130, 246, 0.1));
    }
    button {
      padding: 0.5rem 1rem;
      border: 1px solid var(--table-border-strong);
      border-radius: var(--lumo-border-radius-m, 4px);
      background: var(--button-bg);
      color: var(--table-text);
      cursor: pointer;
      font-family: inherit;
      font-size: var(--lumo-font-size-s, 0.875rem);
    }
    button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    button:hover:not(:disabled) {
      background: var(--button-hover);
    }
    @media (max-width: 768px) {
      .container {
        padding: 0.5rem;
      }
      .header {
        flex-direction: column;
        gap: 0.5rem;
        align-items: stretch;
      }
      .title {
        font-size: 1rem;
      }
      th, td {
        padding: 0.25rem 0.5rem;
        font-size: 0.875rem;
      }
      .pagination {
        font-size: 0.75rem;
      }
      button {
        padding: 0.375rem 0.75rem;
        font-size: 0.875rem;
      }
    }
  `,co([he({type:Array})],fo.prototype,"data",2),co([he({type:Array})],fo.prototype,"columns",2),co([he({type:Number})],fo.prototype,"pageSize",2),co([he({type:String})],fo.prototype,"title",2),co([he({type:String})],fo.prototype,"theme",2),co([he({type:Boolean})],fo.prototype,"selectable",2),co([he({type:Boolean})],fo.prototype,"pageable",2),co([he({type:Boolean})],fo.prototype,"filterable",2),co([he({type:Boolean})],fo.prototype,"actionable",2),co([ce()],fo.prototype,"_filter",1),co([ce()],fo.prototype,"_sortKey",1),co([ce()],fo.prototype,"_sortDir",1),co([ce()],fo.prototype,"_page",1),co([ce()],fo.prototype,"_selectedRow",1),fo=co([le("as-datagrid")],fo);var go=Object.defineProperty,vo=Object.getOwnPropertyDescriptor,yo=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?vo(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&go(t,o,s),s};let $o=class extends se{constructor(){super(...arguments),this.label="",this.value="",this.placeholder="",this.event="event-trigger",this.theme="",this.readonly=!1,this.disabled=!1}render(){const e=this.value||this.placeholder||"";return T`
        <div class="field">
            ${this.label?T`<label>${this.label}</label>`:""}
            <div
                class="event-trigger ${this.readonly?"readonly":""}"
                tabindex="${this.disabled?"-1":"0"}"
                ?disabled="${this.disabled}"
                @click="${this._handleClick}"
                @keydown="${this._handleKeydown}"
            >
                <span>${e}</span>
                <span class="arrow">
                    <svg viewBox="0 0 24 24">
                        <path d="M7 10l5 5 5-5" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </span>
            </div>
        </div>
        `}_handleClick(){this.disabled||this.readonly||this.dispatchEvent(new CustomEvent(this.event,{detail:{value:this.value},bubbles:!0,composed:!0}))}_handleKeydown(e){this.disabled||this.readonly||"Enter"!==e.key&&" "!==e.key||(e.preventDefault(),this._handleClick())}};$o.styles=s`
      :host {
        display: block;
        font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif;
        position: relative;
      }
      .field {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
      }
      label {
        font-size: 0.875rem;
        font-weight: 500;
        color: var(--label-color, #374151);
      }
      .event-trigger {
        padding: 0.5rem 0.75rem;
        border: 1px solid transparent;
        border-radius: 4px;
        font-size: 0.9375rem;
        font-family: inherit;
        background: var(--input-bg, #e8eaed);
        color: var(--text-color, #1a1a1a);
        outline: none;
        cursor: pointer;
        transition: border-color 0.15s;
        display: flex;
        justify-content: space-between;
        align-items: center;
        user-select: none;
      }
      .event-trigger:focus {
        border-color: var(--focus-color, #1676f3);
      }
      .event-trigger:disabled {
        opacity: 0.5;
        cursor: not-allowed;
        background: var(--disabled-bg, #f3f4f6);
      }
      .event-trigger.readonly {
        cursor: default;
        background: var(--readonly-bg, #f9fafb);
      }
      .arrow {
        margin-left: 0.5rem;
        width: 20px;
        height: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      svg {
        width: 20px;
        height: 20px;
        color: var(--text-color, #1f2937);
      }
      :host([theme="dark"]) {
        --label-color: #f3f4f6;
        --border-color: #525252;
        --input-bg: #374151;
        --text-color: #e5e5e5;
        --focus-color: #1676f3;
      }
    `,yo([he({type:String})],$o.prototype,"label",2),yo([he({type:String})],$o.prototype,"value",2),yo([he({type:String})],$o.prototype,"placeholder",2),yo([he({type:String})],$o.prototype,"event",2),yo([he({type:String})],$o.prototype,"theme",2),yo([he({type:Boolean})],$o.prototype,"readonly",2),yo([he({type:Boolean})],$o.prototype,"disabled",2),$o=yo([le("as-event")],$o);var _o,xo,wo,ko,So=Object.defineProperty,Ao=Object.getOwnPropertyDescriptor,Co=e=>{throw TypeError(e)},Eo=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?Ao(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&So(t,o,s),s},Oo=(e,t,o)=>t.has(e)||Co("Cannot "+o),Mo=(e,t,o)=>(Oo(e,t,"read from private field"),o?o.call(e):t.get(e)),Do=(e,t,o)=>t.has(e)?Co("Cannot add the same private member more than once"):t instanceof WeakSet?t.add(e):t.set(e,o),Po=(e,t,o,r)=>(Oo(e,t,"write to private field"),t.set(e,o),o);let zo=class extends se{constructor(){super(...arguments),this.options="label=Editar,value=edit;label=Duplicar,value=duplicate;label=Eliminar,value=delete",this.theme="",this.open=!1,Do(this,_o,0),Do(this,xo,0),Do(this,wo,!1),Do(this,ko,null),this._outsideClickHandler=e=>{this.contains(e.target)||this.hide()}}get _x(){return Mo(this,_o)}set _x(e){Po(this,_o,e)}get _y(){return Mo(this,xo)}set _y(e){Po(this,xo,e)}get _showConfirm(){return Mo(this,wo)}set _showConfirm(e){Po(this,wo,e)}get _pendingItem(){return Mo(this,ko)}set _pendingItem(e){Po(this,ko,e)}get items(){return(new vt).planeDeserialize(this.options)}show(e,t){this._x=e,this._y=t,this.open=!0,this.updateComplete.then(()=>{this._adjustPosition(),this._addOutsideClickListener()})}hide(){this.open=!1,this._removeOutsideClickListener()}_adjustPosition(){const e=this.getBoundingClientRect();let t=this._x-e.width;t<0&&(t=this._x);let o=this._y;o+e.height>window.innerHeight&&(o=this._y-e.height),this.style.left=t+"px",this.style.top=o+"px"}_handleOptionClick(e){this._isDangerOption(e.value)?(this._pendingItem=e,this._showConfirm=!0):this._executeOption(e)}_executeOption(e){this.dispatchEvent(new CustomEvent("option-select",{detail:{value:e.value,label:e.label},bubbles:!0,composed:!0})),this.hide()}_confirmAction(){this._pendingItem&&(this._executeOption(this._pendingItem),this._pendingItem=null),this._showConfirm=!1}_cancelAction(){this._pendingItem=null,this._showConfirm=!1,this.hide()}_addOutsideClickListener(){setTimeout(()=>{document.addEventListener("click",this._outsideClickHandler)},0)}_removeOutsideClickListener(){document.removeEventListener("click",this._outsideClickHandler)}_isDangerOption(e){return["delete","remove","destroy","eliminar","borrar"].some(t=>e.toLowerCase().includes(t))}render(){return T`
            ${this.open?T`
                <div class="popup">
                    ${this.items.map(e=>T`
                        <div 
                            class="option ${this._isDangerOption(e.value)?"danger":""}" 
                            data-value="${e.value}"
                            @click="${()=>this._handleOptionClick(e)}"
                        >
                            ${e.label}
                        </div>
                    `)}
                </div>
            `:""}
            
            ${this._showConfirm&&this._pendingItem?T`
                <div class="overlay" @click="${this._cancelAction}">
                    <div class="dialog" @click="${e=>e.stopPropagation()}">
                        <div class="dialog-header">Confirmar acción</div>
                        <div class="dialog-content">
                            ¿Estás seguro de que deseas ${this._pendingItem.label.toLowerCase()}?
                        </div>
                        <div class="dialog-actions">
                            <button class="btn-cancel" @click="${this._cancelAction}">Cancelar</button>
                            <button class="btn-confirm" @click="${this._confirmAction}">${this._pendingItem.label}</button>
                        </div>
                    </div>
                </div>
            `:""}
        `}};_o=/* @__PURE__ */new WeakMap,xo=/* @__PURE__ */new WeakMap,wo=/* @__PURE__ */new WeakMap,ko=/* @__PURE__ */new WeakMap,zo.styles=s`
        :host {
            position: fixed;
            z-index: 1000;
            font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif;
        }
        .popup {
            background: var(--popup-bg, white);
            border: 1px solid var(--border-color, #ccc);
            border-radius: 4px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.15);
            min-width: 120px;
        }
        .option {
            padding: 0.5rem 0.75rem;
            cursor: pointer;
            font-size: 0.9375rem;
            color: var(--text-color, #1f2937);
            border-bottom: 1px solid var(--border-light, #eee);
            transition: background-color 0.15s;
        }
        .option:last-child {
            border-bottom: none;
        }
        .option:hover {
            background-color: var(--option-hover, #e0f2fe);
        }
        .option.danger {
            color: var(--danger-color, #dc2626);
        }
        .overlay {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0,0,0,0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 10000;
        }
        .dialog {
            background: var(--popup-bg, white);
            border-radius: 8px;
            padding: 1.5rem;
            min-width: 300px;
            max-width: 500px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.2);
            font-family: inherit;
        }
        .dialog-header {
            font-size: 1.125rem;
            font-weight: 600;
            margin-bottom: 1rem;
            color: var(--text-color, #1f2937);
        }
        .dialog-content {
            margin-bottom: 1.5rem;
            color: var(--text-color, #4b5563);
            font-size: 0.9375rem;
        }
        .dialog-actions {
            display: flex;
            gap: 0.5rem;
            justify-content: flex-end;
        }
        .btn-cancel {
            padding: 0.5rem 1rem;
            background: var(--cancel-bg, #e5e7eb);
            color: var(--cancel-text, #1f2937);
            border: none;
            border-radius: 4px;
            font-size: 0.9375rem;
            cursor: pointer;
        }
        .btn-confirm {
            padding: 0.5rem 1rem;
            background: var(--danger-color, #ef4444);
            color: white;
            border: none;
            border-radius: 4px;
            font-size: 0.9375rem;
            cursor: pointer;
        }
        :host([theme="dark"]) {
            --popup-bg: #1f2937;
            --border-color: #374151;
            --border-light: #374151;
            --text-color: #f3f4f6;
            --option-hover: #1e3a5f;
            --danger-color: #ef4444;
        }
    `,Eo([he({type:String})],zo.prototype,"options",2),Eo([he({type:String})],zo.prototype,"theme",2),Eo([he({type:Boolean})],zo.prototype,"open",2),Eo([ce()],zo.prototype,"_x",1),Eo([ce()],zo.prototype,"_y",1),Eo([ce()],zo.prototype,"_showConfirm",1),Eo([ce()],zo.prototype,"_pendingItem",1),Eo([he({type:Array})],zo.prototype,"items",1),zo=Eo([le("as-popup")],zo);var jo=Object.defineProperty,Bo=Object.getOwnPropertyDescriptor,Uo=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?Bo(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&jo(t,o,s),s};let No=class extends se{constructor(){super(...arguments),this.title="",this.open=!1,this.theme="",this._handleKeyDown=e=>{"Escape"===e.key&&this.open&&this.hide()}}show(){this.open=!0,document.addEventListener("keydown",this._handleKeyDown),this.updateComplete.then(()=>{this._notifySlottedForm(!0)})}hide(){this.open=!1,document.removeEventListener("keydown",this._handleKeyDown),this._notifySlottedForm(!1),this.dispatchEvent(new CustomEvent("modal-close",{bubbles:!0,composed:!0}))}_notifySlottedForm(e){const t=this.shadowRoot?.querySelector("slot");if(!t)return;t.assignedElements().forEach(t=>{"as-form"===t.tagName.toLowerCase()&&(t.hideTitle=e,e&&(t._cancelled=!1),"function"==typeof t.requestUpdate&&t.requestUpdate())})}_handleOverlayClick(e){e.target===e.currentTarget&&this.hide()}_handleSlotChange(e){e.target.assignedElements().forEach(e=>{"dark"===this.theme?e.setAttribute("theme","dark"):e.removeAttribute("theme")})}render(){return this.open?T`
            <div class="modal-overlay" @click="${this._handleOverlayClick}">
                <div class="modal-content">
                    <div class="modal-header">
                        <h2 class="modal-title">${this.title}</h2>
                        <button class="close-button" @click="${this.hide}">
                            <svg class="close-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M18 6L6 18M6 6l12 12"/>
                            </svg>
                        </button>
                    </div>
                    <div class="modal-body">
                        <slot @slotchange="${this._handleSlotChange}"></slot>
                    </div>
                </div>
            </div>
        `:T``}};No.styles=s`
        :host {
            font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif;
        }
        .modal-overlay {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0,0,0,0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 9999;
        }
        .modal-content {
            background: white;
            border-radius: 8px;
            padding: 1.5rem;
            min-width: 300px;
            max-width: 500px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.2);
            font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif;
            position: relative;
            max-height: 90vh;
            overflow-y: auto;
        }
        .modal-header {
            font-size: 1.125rem;
            font-weight: 600;
            margin-bottom: 1rem;
            color: #1f2937;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .modal-title {
            font-size: 1.125rem;
            font-weight: 600;
            color: #1f2937;
        }
        .close-button {
            background: none;
            border: none;
            font-size: 1.5rem;
            cursor: pointer;
            color: #6b7280;
            padding: 0;
            width: 24px;
            height: 24px;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .close-button:hover {
            color: #1f2937;
        }
        .close-icon {
            width: 1.25rem;
            height: 1.25rem;
        }
        .modal-body {
            color: #4b5563;
            font-size: 0.9375rem;
        }
        .hidden {
            display: none;
        }
        :host([theme="dark"]) .modal-content {
            background: #1f2937;
            color: #f3f4f6;
        }
        :host([theme="dark"]) .modal-title {
            color: #f3f4f6;
        }
        :host([theme="dark"]) .close-button {
            color: #9ca3af;
        }
        :host([theme="dark"]) .close-button:hover {
            color: #d1d5db;
        }
        :host([theme="dark"]) .modal-body {
            color: #d1d5db;
        }
    `,Uo([he({type:String})],No.prototype,"title",2),Uo([he({type:Boolean})],No.prototype,"open",2),Uo([he({type:String})],No.prototype,"theme",2),No=Uo([le("as-modal")],No);var Fo,Ro,Ho,To=Object.defineProperty,Io=Object.getOwnPropertyDescriptor,qo=e=>{throw TypeError(e)},Lo=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?Io(t,o):t,a=e.length-1;a>=0;a--)(i=e[a])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&To(t,o,s),s},Wo=(e,t,o)=>t.has(e)||qo("Cannot "+o),Vo=(e,t,o)=>(Wo(e,t,"read from private field"),o?o.call(e):t.get(e)),Ko=(e,t,o)=>t.has(e)?qo("Cannot add the same private member more than once"):t instanceof WeakSet?t.add(e):t.set(e,o),Jo=(e,t,o,r)=>(Wo(e,t,"write to private field"),t.set(e,o),o);let Yo=class{constructor(){this.validators=/* @__PURE__ */new Map,this.setupDefaultValidators()}setupDefaultValidators(){this.validators.set("required",{validate:e=>e&&e.toString().trim().length>0,message:"This field is required"}),this.validators.set("email",{validate:e=>!e||/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(e),message:"Please enter a valid email address"}),this.validators.set("min",{validate:(e,t)=>!e||e.toString().length>=parseInt(t),message:e=>`Minimum ${e} characters required`}),this.validators.set("max",{validate:(e,t)=>!e||e.toString().length<=parseInt(t),message:e=>`Maximum ${e} characters allowed`}),this.validators.set("enum",{validate:(e,t)=>!e||t.split(",").includes(e),message:e=>`Value must be one of: ${e.replace(/,/g,", ")}`})}validateField(e,t){if(!t)return{valid:!0};for(const o of t){const[t,r]=o.split(":"),i=this.validators.get(t);if(i&&!i.validate(e,r)){return{valid:!1,message:"function"==typeof i.message?i.message(r):i.message}}}return{valid:!0}}},Xo=class extends se{constructor(){super(...arguments),this.schema={},this.theme="",this.successMessage="",this.errorMessage="",this.hideTitle=!1,Ko(this,Fo,{}),Ko(this,Ro,{}),Ko(this,Ho,!1),this._formBuilder=new Yo,this._handleKeyDown=e=>{"Escape"===e.key?this._handleCancel():"Enter"!==e.key||e.shiftKey||(e.preventDefault(),this._handleSubmit())}}get _formData(){return Vo(this,Fo)}set _formData(e){Jo(this,Fo,e)}get _errors(){return Vo(this,Ro)}set _errors(e){Jo(this,Ro,e)}get _cancelled(){return Vo(this,Ho)}set _cancelled(e){Jo(this,Ho,e)}_renderField(e){const t=this._formData[e.name]||e.value||"",o=this._errors[e.name];switch(e.type){case"text":case"email":case"password":case"number":return T`
                    <as-input
                        label="${e.label||""}"
                        kind="${e.type}"
                        value="${t}"
                        placeholder="${e.placeholder||""}"
                        ?required="${e.required}"
                        ?readonly="${e.readonly}"
                        ?disabled="${e.disabled}"
                        theme="${this.theme}"
                        @value-changed="${t=>this._handleFieldChange(e.name,t.detail.value,e.validation)}"
                    ></as-input>
                    ${o?T`<div class="error-message">${o}</div>`:""}
                `;case"textarea":return T`
                    <as-text
                        label="${e.label||""}"
                        value="${t}"
                        placeholder="${e.placeholder||""}"
                        rows="${e.rows||3}"
                        ?required="${e.required}"
                        ?readonly="${e.readonly}"
                        ?disabled="${e.disabled}"
                        theme="${this.theme}"
                        @value-changed="${t=>this._handleFieldChange(e.name,t.detail.value,e.validation)}"
                    ></as-text>
                    ${o?T`<div class="error-message">${o}</div>`:""}
                `;case"select":return T`
                    <as-select
                        label="${e.label||""}"
                        value="${t}"
                        options="${this._formatOptions(e.options)}"
                        ?required="${e.required}"
                        ?disabled="${e.disabled}"
                        theme="${this.theme}"
                        @value-changed="${t=>this._handleFieldChange(e.name,t.detail.value,e.validation)}"
                    ></as-select>
                    ${o?T`<div class="error-message">${o}</div>`:""}
                `;case"checkbox":return T`
                    <as-check
                        label="${e.label||""}"
                        ?checked="${t}"
                        ?disabled="${e.disabled}"
                        theme="${this.theme}"
                        @checked-changed="${t=>this._handleFieldChange(e.name,t.detail.value,e.validation)}"
                    ></as-check>
                    ${o?T`<div class="error-message">${o}</div>`:""}
                `;case"switch":return T`
                    <as-switch
                        label="${e.label||""}"
                        ?checked="${t}"
                        ?disabled="${e.disabled}"
                        theme="${this.theme}"
                        @checked-changed="${t=>this._handleFieldChange(e.name,t.detail.value,e.validation)}"
                    ></as-switch>
                    ${o?T`<div class="error-message">${o}</div>`:""}
                `;case"radio":return T`
                    <as-radio
                        label="${e.label||""}"
                        value="${t}"
                        options="${this._formatOptions(e.options)}"
                        ?disabled="${e.disabled}"
                        theme="${this.theme}"
                        @value-changed="${t=>this._handleFieldChange(e.name,t.detail.value,e.validation)}"
                    ></as-radio>
                    ${o?T`<div class="error-message">${o}</div>`:""}
                `;case"date":return T`
                    <as-date
                        label="${e.label||""}"
                        value="${t}"
                        placeholder="${e.placeholder||""}"
                        ?required="${e.required}"
                        ?readonly="${e.readonly}"
                        ?disabled="${e.disabled}"
                        theme="${this.theme}"
                        @value-changed="${t=>this._handleFieldChange(e.name,t.detail.value,e.validation)}"
                    ></as-date>
                    ${o?T`<div class="error-message">${o}</div>`:""}
                `;case"time":return T`
                    <as-time
                        label="${e.label||""}"
                        value="${t}"
                        placeholder="${e.placeholder||""}"
                        ?required="${e.required}"
                        ?readonly="${e.readonly}"
                        ?disabled="${e.disabled}"
                        theme="${this.theme}"
                        @value-changed="${t=>this._handleFieldChange(e.name,t.detail.value,e.validation)}"
                    ></as-time>
                    ${o?T`<div class="error-message">${o}</div>`:""}
                `;case"complete":return T`
                    <as-complete
                        label="${e.label||""}"
                        value="${t}"
                        options="${this._formatOptions(e.options)}"
                        placeholder="${e.placeholder||""}"
                        ?required="${e.required}"
                        ?disabled="${e.disabled}"
                        theme="${this.theme}"
                        @value-changed="${t=>this._handleFieldChange(e.name,t.detail.value,e.validation)}"
                    ></as-complete>
                    ${o?T`<div class="error-message">${o}</div>`:""}
                `;default:return T`<div>Unsupported field type: ${e.type}</div>`}}_formatOptions(e){return e?e.map(e=>`label=${e.label},value=${e.value}`).join(";"):""}_handleFieldChange(e,t,o){if(this._formData={...this._formData,[e]:t},this._errors[e]){const t={...this._errors};delete t[e],this._errors=t}if(o){const r=this._formBuilder.validateField(t,o);r.valid||(this._errors={...this._errors,[e]:r.message})}this.dispatchEvent(new CustomEvent("field-change",{detail:{fieldName:e,value:t,formData:this._formData},bubbles:!0,composed:!0}))}_handleSubmit(){let e=!1;const t={};[...this.schema.fields||[],...this.schema.sections?.flatMap(e=>e.fields)||[]].forEach(o=>{if(o.validation){const r=this._formData[o.name]||"",i=this._formBuilder.validateField(r,o.validation);i.valid||(t[o.name]=i.message,e=!0)}}),this._errors=t,e?this.errorMessage&&this.showNotification(this.errorMessage,"error"):(this.successMessage&&this.showNotification(this.successMessage),this.dispatchEvent(new CustomEvent("form-submit",{detail:{formData:this._formData},bubbles:!0,composed:!0})))}_handleCancel(){this._cancelled=!0,this.dispatchEvent(new CustomEvent("form-cancel",{bubbles:!0,composed:!0}))}_focusFirstInput(){this.updateComplete.then(()=>{const e=this.shadowRoot?.querySelector("as-input, as-text, as-select, as-date, as-time, as-complete");e&&"function"==typeof e.focus&&e.focus()})}connectedCallback(){super.connectedCallback(),document.addEventListener("keydown",this._handleKeyDown),this._focusFirstInput()}disconnectedCallback(){super.disconnectedCallback(),document.removeEventListener("keydown",this._handleKeyDown)}render(){return this.schema&&(this.schema.fields||this.schema.sections)?T`
            <div class="form-container">
                ${this.schema.title&&!this.hideTitle?T`<h2 class="form-title">${this.schema.title}</h2>`:""}
                
                ${this.schema.sections?this.schema.sections.map(e=>T`
                        <div class="form-section">
                            ${e.title?T`<h3 class="section-title">${e.title}</h3>`:""}
                            ${e.fields?.map(e=>this._renderField(e))}
                        </div>
                    `):this.schema.fields?.map(e=>this._renderField(e))}
                
                ${this.schema.skipActions?"":T`
                    <div class="form-actions">
                        ${this.schema.hideCancelButton?"":T`
                            <as-button 
                                label="${this.schema.cancelLabel||"Cancel"}"
                                variant="secondary"
                                theme="${this.theme}"
                                @button-tap="${this._handleCancel}"
                            ></as-button>
                        `}
                        <as-button 
                            label="${this.schema.submitLabel||"Save"}"
                            variant="primary"
                            ?disabled="${this._cancelled}"
                            theme="${this.theme}"
                            @button-tap="${this._handleSubmit}"
                        ></as-button>
                    </div>
                `}
            </div>
        `:T`<div>No form schema provided</div>`}getFormData(){return{...this._formData}}setFormData(e){this._formData={...e},this.requestUpdate()}clearErrors(){this._errors={},this.requestUpdate()}validate(){return this._handleSubmit(),0===Object.keys(this._errors).length}focus(){this._focusFirstInput()}showNotification(e,t="success"){const o=document.createElement("div");o.textContent=e;const r="error"===t?"#dc2626":"#059669";o.style.cssText=`position:fixed;bottom:20px;left:50%;transform:translateX(-50%);background:${r};color:white;padding:0.75rem 1.5rem;border-radius:4px;box-shadow:0 4px 6px rgba(0,0,0,0.1);z-index:9999;`,document.body.appendChild(o),setTimeout(()=>o.remove(),3500)}};Fo=/* @__PURE__ */new WeakMap,Ro=/* @__PURE__ */new WeakMap,Ho=/* @__PURE__ */new WeakMap,Xo.styles=s`
        :host {
            display: block;
            font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif;
        }
        .form-container {
            display: flex;
            flex-direction: column;
            gap: 1rem;
        }
        .form-title {
            font-size: 1.5rem;
            font-weight: 600;
            margin-bottom: 1rem;
            color: var(--text-color, #1f2937);
        }
        .form-section {
            display: flex;
            flex-direction: column;
            gap: 0.75rem;
        }
        .section-title {
            font-size: 1.125rem;
            font-weight: 500;
            color: var(--text-color, #374151);
            margin-bottom: 0.5rem;
        }
        .form-actions {
            display: flex;
            gap: 0.75rem;
            justify-content: flex-end;
            margin-top: 1.5rem;
            padding-top: 1rem;
            border-top: 1px solid var(--border-color, #e5e7eb);
        }
        .error-message {
            color: var(--error-color, #dc2626);
            font-size: 0.875rem;
            margin-top: 0.25rem;
        }
        :host([theme="dark"]) {
            --text-color: #f3f4f6;
            --border-color: #374151;
            --error-color: #ef4444;
        }
    `,Lo([he({type:Object})],Xo.prototype,"schema",2),Lo([he({type:String})],Xo.prototype,"theme",2),Lo([he({type:String})],Xo.prototype,"successMessage",2),Lo([he({type:String})],Xo.prototype,"errorMessage",2),Lo([he({type:Boolean})],Xo.prototype,"hideTitle",2),Lo([ce()],Xo.prototype,"_formData",1),Lo([ce()],Xo.prototype,"_errors",1),Lo([ce()],Xo.prototype,"_cancelled",1),Xo=Lo([le("as-form")],Xo);class Zo{constructor(e={}){this.validators=/* @__PURE__ */new Map,this.config={autoValidation:!1!==e.autoValidation,customValidators:e.customValidators||{},...e},this.init()}init(){this.setupDefaultValidators(),this.setupCustomValidators()}setupDefaultValidators(){this.validators.set("required",{validate:e=>e&&e.toString().trim().length>0,message:"This field is required"}),this.validators.set("email",{validate:e=>!e||/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(e),message:"Please enter a valid email address"}),this.validators.set("min",{validate:(e,t)=>!e||e.toString().length>=parseInt(t),message:e=>`Minimum ${e} characters required`}),this.validators.set("max",{validate:(e,t)=>!e||e.toString().length<=parseInt(t),message:e=>`Maximum ${e} characters allowed`}),this.validators.set("number",{validate:e=>!e||!isNaN(Number(e)),message:"Please enter a valid number"}),this.validators.set("positive",{validate:e=>!e||Number(e)>0,message:"Please enter a positive number"}),this.validators.set("url",{validate:e=>!e||/^https?:\/\/.+/.test(e),message:"Please enter a valid URL"}),this.validators.set("pattern",{validate:(e,t)=>!e||new RegExp(t).test(e),message:e=>`Value must match pattern: ${e}`}),this.validators.set("enum",{validate:(e,t)=>!e||t.split(",").includes(e),message:e=>`Value must be one of: ${e.replace(/,/g,", ")}`}),this.validators.set("password",{validate:e=>!e||/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/.test(e),message:"Password must be at least 8 characters with uppercase, lowercase and number"})}setupCustomValidators(){Object.entries(this.config.customValidators).forEach(([e,t])=>{this.validators.set(e,t)})}addValidator(e,t){this.validators.set(e,t)}validateField(e,t){if(!t||!Array.isArray(t))return{valid:!0};for(const o of t){const[t,r]=o.split(":"),i=this.validators.get(t);if(i&&!i.validate(e,r)){return{valid:!1,message:"function"==typeof i.message?i.message(r):i.message}}}return{valid:!0}}validateForm(e,t){const o={};return this.getAllFields(t).forEach(t=>{if(t.validation){const r=e[t.name]||"",i=this.validateField(r,t.validation);i.valid||(o[t.name]=i.message)}}),{valid:0===Object.keys(o).length,errors:o}}getAllFields(e){const t=[];return e.fields&&t.push(...e.fields),e.sections&&e.sections.forEach(e=>{e.fields&&t.push(...e.fields)}),t}createFormSchema(e){return{title:e.title||"",sections:e.sections||null,fields:e.fields||[],submitLabel:e.submitLabel||"Save",cancelLabel:e.cancelLabel||"Cancel",hideCancelButton:e.hideCancelButton||!1,skipActions:e.skipActions||!1,...e}}createUserFormSchema(e={}){return this.createFormSchema({title:"User Information",fields:[{name:"username",type:"text",label:"Username",value:e.username||"",required:!0,validation:["required","min:3"]},{name:"email",type:"email",label:"Email",value:e.email||"",required:!0,validation:["required","email"]},{name:"firstName",type:"text",label:"First Name",value:e.firstName||"",required:!0,validation:["required"]},{name:"lastName",type:"text",label:"Last Name",value:e.lastName||"",required:!0,validation:["required"]},{name:"enabled",type:"switch",label:"Account Enabled",value:void 0===e.enabled||e.enabled}]})}createContactFormSchema(e={}){return this.createFormSchema({title:"Contact Information",sections:[{title:"Personal Information",fields:[{name:"name",type:"text",label:"Full Name",value:e.name||"",required:!0,validation:["required","min:2"]},{name:"email",type:"email",label:"Email Address",value:e.email||"",required:!0,validation:["required","email"]},{name:"phone",type:"text",label:"Phone Number",value:e.phone||"",placeholder:"+1 (555) 123-4567"}]},{title:"Message",fields:[{name:"subject",type:"select",label:"Subject",value:e.subject||"",required:!0,validation:["required"],options:[{label:"General Inquiry",value:"general"},{label:"Support Request",value:"support"},{label:"Bug Report",value:"bug"},{label:"Feature Request",value:"feature"}]},{name:"message",type:"textarea",label:"Message",value:e.message||"",required:!0,validation:["required","min:10"],rows:5,placeholder:"Please describe your inquiry..."}]}]})}createSettingsFormSchema(e={}){return this.createFormSchema({title:"Application Settings",sections:[{title:"General Settings",fields:[{name:"appName",type:"text",label:"Application Name",value:e.appName||"",required:!0,validation:["required"]},{name:"theme",type:"radio",label:"Theme",value:e.theme||"light",options:[{label:"Light",value:"light"},{label:"Dark",value:"dark"},{label:"Auto",value:"auto"}]}]},{title:"Notifications",fields:[{name:"emailNotifications",type:"switch",label:"Email Notifications",value:void 0===e.emailNotifications||e.emailNotifications},{name:"pushNotifications",type:"switch",label:"Push Notifications",value:void 0!==e.pushNotifications&&e.pushNotifications}]}]})}formatOptionsString(e){return e&&Array.isArray(e)?e.map(e=>`label=${e.label},value=${e.value}`).join(";"):""}parseOptionsString(e){return e?e.split(";").map(e=>{const[t,o]=e.split(",");return{label:t.replace("label=",""),value:o.replace("value=","")}}):[]}processFormData(e,t){const o={...e};return this.getAllFields(t).forEach(e=>{const t=o[e.name];"number"===e.type&&t?o[e.name]=Number(t):"checkbox"!==e.type&&"switch"!==e.type||(o[e.name]=Boolean(t))}),o}}"undefined"!=typeof window&&(window.AsFormBuilder=Zo);export{be as AsBox,ye as AsButton,Ce as AsCheck,Ht as AsComplete,Fe as AsConfirm,fo as AsDatagrid,Xe as AsDate,ut as AsEmbed,$o as AsEvent,Xo as AsForm,Zo as AsFormBuilder,gt as AsImage,we as AsInput,No as AsModal,zo as AsPopup,xt as AsRadio,Ot as AsSelect,De as AsSwitch,Lt as AsText,dt as AsTime,Zt as AsUpload,to as AsVideo};
