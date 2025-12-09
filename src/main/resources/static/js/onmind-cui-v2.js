const e=globalThis,t=e.ShadowRoot&&(void 0===e.ShadyCSS||e.ShadyCSS.nativeShadow)&&"adoptedStyleSheets"in Document.prototype&&"replace"in CSSStyleSheet.prototype,o=Symbol(),r=/* @__PURE__ */new WeakMap;let i=class{constructor(e,t,r){if(this._$cssResult$=!0,r!==o)throw Error("CSSResult is not constructable. Use `unsafeCSS` or `css` instead.");this.cssText=e,this.t=t}get styleSheet(){let e=this.o;const o=this.t;if(t&&void 0===e){const t=void 0!==o&&1===o.length;t&&(e=r.get(o)),void 0===e&&((this.o=e=new CSSStyleSheet).replaceSync(this.cssText),t&&r.set(o,e))}return e}toString(){return this.cssText}};const s=(e,...t)=>{const r=1===e.length?e[0]:t.reduce((t,o,r)=>t+(e=>{if(!0===e._$cssResult$)return e.cssText;if("number"==typeof e)return e;throw Error("Value passed to 'css' function must be a 'css' function result: "+e+". Use 'unsafeCSS' to pass non-literal values, but take care to ensure page security.")})(o)+e[r+1],e[0]);return new i(r,e,o)},n=t?e=>e:e=>e instanceof CSSStyleSheet?(e=>{let t="";for(const o of e.cssRules)t+=o.cssText;return(e=>new i("string"==typeof e?e:e+"",void 0,o))(t)})(e):e,{is:a,defineProperty:l,getOwnPropertyDescriptor:d,getOwnPropertyNames:c,getOwnPropertySymbols:h,getPrototypeOf:p}=Object,u=globalThis,b=u.trustedTypes,g=b?b.emptyScript:"",f=u.reactiveElementPolyfillSupport,m=(e,t)=>e,v={toAttribute(e,t){switch(t){case Boolean:e=e?g:null;break;case Object:case Array:e=null==e?e:JSON.stringify(e)}return e},fromAttribute(e,t){let o=e;switch(t){case Boolean:o=null!==e;break;case Number:o=null===e?null:Number(e);break;case Object:case Array:try{o=JSON.parse(e)}catch(r){o=null}}return o}},y=(e,t)=>!a(e,t),$={attribute:!0,type:String,converter:v,reflect:!1,useDefault:!1,hasChanged:y};Symbol.metadata??=Symbol("metadata"),u.litPropertyMetadata??=/* @__PURE__ */new WeakMap;let _=class extends HTMLElement{static addInitializer(e){this._$Ei(),(this.l??=[]).push(e)}static get observedAttributes(){return this.finalize(),this._$Eh&&[...this._$Eh.keys()]}static createProperty(e,t=$){if(t.state&&(t.attribute=!1),this._$Ei(),this.prototype.hasOwnProperty(e)&&((t=Object.create(t)).wrapped=!0),this.elementProperties.set(e,t),!t.noAccessor){const o=Symbol(),r=this.getPropertyDescriptor(e,o,t);void 0!==r&&l(this.prototype,e,r)}}static getPropertyDescriptor(e,t,o){const{get:r,set:i}=d(this.prototype,e)??{get(){return this[t]},set(e){this[t]=e}};return{get:r,set(t){const s=r?.call(this);i?.call(this,t),this.requestUpdate(e,s,o)},configurable:!0,enumerable:!0}}static getPropertyOptions(e){return this.elementProperties.get(e)??$}static _$Ei(){if(this.hasOwnProperty(m("elementProperties")))return;const e=p(this);e.finalize(),void 0!==e.l&&(this.l=[...e.l]),this.elementProperties=new Map(e.elementProperties)}static finalize(){if(this.hasOwnProperty(m("finalized")))return;if(this.finalized=!0,this._$Ei(),this.hasOwnProperty(m("properties"))){const e=this.properties,t=[...c(e),...h(e)];for(const o of t)this.createProperty(o,e[o])}const e=this[Symbol.metadata];if(null!==e){const t=litPropertyMetadata.get(e);if(void 0!==t)for(const[e,o]of t)this.elementProperties.set(e,o)}this._$Eh=/* @__PURE__ */new Map;for(const[t,o]of this.elementProperties){const e=this._$Eu(t,o);void 0!==e&&this._$Eh.set(e,t)}this.elementStyles=this.finalizeStyles(this.styles)}static finalizeStyles(e){const t=[];if(Array.isArray(e)){const o=new Set(e.flat(1/0).reverse());for(const e of o)t.unshift(n(e))}else void 0!==e&&t.push(n(e));return t}static _$Eu(e,t){const o=t.attribute;return!1===o?void 0:"string"==typeof o?o:"string"==typeof e?e.toLowerCase():void 0}constructor(){super(),this._$Ep=void 0,this.isUpdatePending=!1,this.hasUpdated=!1,this._$Em=null,this._$Ev()}_$Ev(){this._$ES=new Promise(e=>this.enableUpdating=e),this._$AL=/* @__PURE__ */new Map,this._$E_(),this.requestUpdate(),this.constructor.l?.forEach(e=>e(this))}addController(e){(this._$EO??=/* @__PURE__ */new Set).add(e),void 0!==this.renderRoot&&this.isConnected&&e.hostConnected?.()}removeController(e){this._$EO?.delete(e)}_$E_(){const e=/* @__PURE__ */new Map,t=this.constructor.elementProperties;for(const o of t.keys())this.hasOwnProperty(o)&&(e.set(o,this[o]),delete this[o]);e.size>0&&(this._$Ep=e)}createRenderRoot(){const o=this.shadowRoot??this.attachShadow(this.constructor.shadowRootOptions);return((o,r)=>{if(t)o.adoptedStyleSheets=r.map(e=>e instanceof CSSStyleSheet?e:e.styleSheet);else for(const t of r){const r=document.createElement("style"),i=e.litNonce;void 0!==i&&r.setAttribute("nonce",i),r.textContent=t.cssText,o.appendChild(r)}})(o,this.constructor.elementStyles),o}connectedCallback(){this.renderRoot??=this.createRenderRoot(),this.enableUpdating(!0),this._$EO?.forEach(e=>e.hostConnected?.())}enableUpdating(e){}disconnectedCallback(){this._$EO?.forEach(e=>e.hostDisconnected?.())}attributeChangedCallback(e,t,o){this._$AK(e,o)}_$ET(e,t){const o=this.constructor.elementProperties.get(e),r=this.constructor._$Eu(e,o);if(void 0!==r&&!0===o.reflect){const i=(void 0!==o.converter?.toAttribute?o.converter:v).toAttribute(t,o.type);this._$Em=e,null==i?this.removeAttribute(r):this.setAttribute(r,i),this._$Em=null}}_$AK(e,t){const o=this.constructor,r=o._$Eh.get(e);if(void 0!==r&&this._$Em!==r){const e=o.getPropertyOptions(r),i="function"==typeof e.converter?{fromAttribute:e.converter}:void 0!==e.converter?.fromAttribute?e.converter:v;this._$Em=r;const s=i.fromAttribute(t,e.type);this[r]=s??this._$Ej?.get(r)??s,this._$Em=null}}requestUpdate(e,t,o){if(void 0!==e){const r=this.constructor,i=this[e];if(o??=r.getPropertyOptions(e),!((o.hasChanged??y)(i,t)||o.useDefault&&o.reflect&&i===this._$Ej?.get(e)&&!this.hasAttribute(r._$Eu(e,o))))return;this.C(e,t,o)}!1===this.isUpdatePending&&(this._$ES=this._$EP())}C(e,t,{useDefault:o,reflect:r,wrapped:i},s){o&&!(this._$Ej??=/* @__PURE__ */new Map).has(e)&&(this._$Ej.set(e,s??t??this[e]),!0!==i||void 0!==s)||(this._$AL.has(e)||(this.hasUpdated||o||(t=void 0),this._$AL.set(e,t)),!0===r&&this._$Em!==e&&(this._$Eq??=/* @__PURE__ */new Set).add(e))}async _$EP(){this.isUpdatePending=!0;try{await this._$ES}catch(t){Promise.reject(t)}const e=this.scheduleUpdate();return null!=e&&await e,!this.isUpdatePending}scheduleUpdate(){return this.performUpdate()}performUpdate(){if(!this.isUpdatePending)return;if(!this.hasUpdated){if(this.renderRoot??=this.createRenderRoot(),this._$Ep){for(const[e,t]of this._$Ep)this[e]=t;this._$Ep=void 0}const e=this.constructor.elementProperties;if(e.size>0)for(const[t,o]of e){const{wrapped:e}=o,r=this[t];!0!==e||this._$AL.has(t)||void 0===r||this.C(t,void 0,o,r)}}let e=!1;const t=this._$AL;try{e=this.shouldUpdate(t),e?(this.willUpdate(t),this._$EO?.forEach(e=>e.hostUpdate?.()),this.update(t)):this._$EM()}catch(o){throw e=!1,this._$EM(),o}e&&this._$AE(t)}willUpdate(e){}_$AE(e){this._$EO?.forEach(e=>e.hostUpdated?.()),this.hasUpdated||(this.hasUpdated=!0,this.firstUpdated(e)),this.updated(e)}_$EM(){this._$AL=/* @__PURE__ */new Map,this.isUpdatePending=!1}get updateComplete(){return this.getUpdateComplete()}getUpdateComplete(){return this._$ES}shouldUpdate(e){return!0}update(e){this._$Eq&&=this._$Eq.forEach(e=>this._$ET(e,this[e])),this._$EM()}updated(e){}firstUpdated(e){}};_.elementStyles=[],_.shadowRootOptions={mode:"open"},_[m("elementProperties")]=/* @__PURE__ */new Map,_[m("finalized")]=/* @__PURE__ */new Map,f?.({ReactiveElement:_}),(u.reactiveElementVersions??=[]).push("2.1.1");const x=globalThis,w=x.trustedTypes,k=w?w.createPolicy("lit-html",{createHTML:e=>e}):void 0,S="$lit$",A=`lit$${Math.random().toFixed(9).slice(2)}$`,E="?"+A,C=`<${E}>`,O=document,M=()=>O.createComment(""),P=e=>null===e||"object"!=typeof e&&"function"!=typeof e,z=Array.isArray,j="[ \t\n\f\r]",D=/<(?:(!--|\/[^a-zA-Z])|(\/?[a-zA-Z][^>\s]*)|(\/?$))/g,U=/-->/g,H=/>/g,R=RegExp(`>|${j}(?:([^\\s"'>=/]+)(${j}*=${j}*(?:[^ \t\n\f\r"'\`<>=]|("|')|))|$)`,"g"),T=/'/g,B=/"/g,N=/^(?:script|style|textarea|title)$/i,W=(K=1,(e,...t)=>({_$litType$:K,strings:e,values:t})),I=Symbol.for("lit-noChange"),F=Symbol.for("lit-nothing"),L=/* @__PURE__ */new WeakMap,V=O.createTreeWalker(O,129);var K;function q(e,t){if(!z(e)||!e.hasOwnProperty("raw"))throw Error("invalid template strings array");return void 0!==k?k.createHTML(t):t}class J{constructor({strings:e,_$litType$:t},o){let r;this.parts=[];let i=0,s=0;const n=e.length-1,a=this.parts,[l,d]=((e,t)=>{const o=e.length-1,r=[];let i,s=2===t?"<svg>":3===t?"<math>":"",n=D;for(let a=0;a<o;a++){const t=e[a];let o,l,d=-1,c=0;for(;c<t.length&&(n.lastIndex=c,l=n.exec(t),null!==l);)c=n.lastIndex,n===D?"!--"===l[1]?n=U:void 0!==l[1]?n=H:void 0!==l[2]?(N.test(l[2])&&(i=RegExp("</"+l[2],"g")),n=R):void 0!==l[3]&&(n=R):n===R?">"===l[0]?(n=i??D,d=-1):void 0===l[1]?d=-2:(d=n.lastIndex-l[2].length,o=l[1],n=void 0===l[3]?R:'"'===l[3]?B:T):n===B||n===T?n=R:n===U||n===H?n=D:(n=R,i=void 0);const h=n===R&&e[a+1].startsWith("/>")?" ":"";s+=n===D?t+C:d>=0?(r.push(o),t.slice(0,d)+S+t.slice(d)+A+h):t+A+(-2===d?a:h)}return[q(e,s+(e[o]||"<?>")+(2===t?"</svg>":3===t?"</math>":"")),r]})(e,t);if(this.el=J.createElement(l,o),V.currentNode=this.el.content,2===t||3===t){const e=this.el.content.firstChild;e.replaceWith(...e.childNodes)}for(;null!==(r=V.nextNode())&&a.length<n;){if(1===r.nodeType){if(r.hasAttributes())for(const e of r.getAttributeNames())if(e.endsWith(S)){const t=d[s++],o=r.getAttribute(e).split(A),n=/([.?@])?(.*)/.exec(t);a.push({type:1,index:i,name:n[2],strings:o,ctor:"."===n[1]?Q:"?"===n[1]?ee:"@"===n[1]?te:G}),r.removeAttribute(e)}else e.startsWith(A)&&(a.push({type:6,index:i}),r.removeAttribute(e));if(N.test(r.tagName)){const e=r.textContent.split(A),t=e.length-1;if(t>0){r.textContent=w?w.emptyScript:"";for(let o=0;o<t;o++)r.append(e[o],M()),V.nextNode(),a.push({type:2,index:++i});r.append(e[t],M())}}}else if(8===r.nodeType)if(r.data===E)a.push({type:2,index:i});else{let e=-1;for(;-1!==(e=r.data.indexOf(A,e+1));)a.push({type:7,index:i}),e+=A.length-1}i++}}static createElement(e,t){const o=O.createElement("template");return o.innerHTML=e,o}}function Y(e,t,o=e,r){if(t===I)return t;let i=void 0!==r?o._$Co?.[r]:o._$Cl;const s=P(t)?void 0:t._$litDirective$;return i?.constructor!==s&&(i?._$AO?.(!1),void 0===s?i=void 0:(i=new s(e),i._$AT(e,o,r)),void 0!==r?(o._$Co??=[])[r]=i:o._$Cl=i),void 0!==i&&(t=Y(e,i._$AS(e,t.values),i,r)),t}class X{constructor(e,t){this._$AV=[],this._$AN=void 0,this._$AD=e,this._$AM=t}get parentNode(){return this._$AM.parentNode}get _$AU(){return this._$AM._$AU}u(e){const{el:{content:t},parts:o}=this._$AD,r=(e?.creationScope??O).importNode(t,!0);V.currentNode=r;let i=V.nextNode(),s=0,n=0,a=o[0];for(;void 0!==a;){if(s===a.index){let t;2===a.type?t=new Z(i,i.nextSibling,this,e):1===a.type?t=new a.ctor(i,a.name,a.strings,this,e):6===a.type&&(t=new oe(i,this,e)),this._$AV.push(t),a=o[++n]}s!==a?.index&&(i=V.nextNode(),s++)}return V.currentNode=O,r}p(e){let t=0;for(const o of this._$AV)void 0!==o&&(void 0!==o.strings?(o._$AI(e,o,t),t+=o.strings.length-2):o._$AI(e[t])),t++}}class Z{get _$AU(){return this._$AM?._$AU??this._$Cv}constructor(e,t,o,r){this.type=2,this._$AH=F,this._$AN=void 0,this._$AA=e,this._$AB=t,this._$AM=o,this.options=r,this._$Cv=r?.isConnected??!0}get parentNode(){let e=this._$AA.parentNode;const t=this._$AM;return void 0!==t&&11===e?.nodeType&&(e=t.parentNode),e}get startNode(){return this._$AA}get endNode(){return this._$AB}_$AI(e,t=this){e=Y(this,e,t),P(e)?e===F||null==e||""===e?(this._$AH!==F&&this._$AR(),this._$AH=F):e!==this._$AH&&e!==I&&this._(e):void 0!==e._$litType$?this.$(e):void 0!==e.nodeType?this.T(e):(e=>z(e)||"function"==typeof e?.[Symbol.iterator])(e)?this.k(e):this._(e)}O(e){return this._$AA.parentNode.insertBefore(e,this._$AB)}T(e){this._$AH!==e&&(this._$AR(),this._$AH=this.O(e))}_(e){this._$AH!==F&&P(this._$AH)?this._$AA.nextSibling.data=e:this.T(O.createTextNode(e)),this._$AH=e}$(e){const{values:t,_$litType$:o}=e,r="number"==typeof o?this._$AC(e):(void 0===o.el&&(o.el=J.createElement(q(o.h,o.h[0]),this.options)),o);if(this._$AH?._$AD===r)this._$AH.p(t);else{const e=new X(r,this),o=e.u(this.options);e.p(t),this.T(o),this._$AH=e}}_$AC(e){let t=L.get(e.strings);return void 0===t&&L.set(e.strings,t=new J(e)),t}k(e){z(this._$AH)||(this._$AH=[],this._$AR());const t=this._$AH;let o,r=0;for(const i of e)r===t.length?t.push(o=new Z(this.O(M()),this.O(M()),this,this.options)):o=t[r],o._$AI(i),r++;r<t.length&&(this._$AR(o&&o._$AB.nextSibling,r),t.length=r)}_$AR(e=this._$AA.nextSibling,t){for(this._$AP?.(!1,!0,t);e!==this._$AB;){const t=e.nextSibling;e.remove(),e=t}}setConnected(e){void 0===this._$AM&&(this._$Cv=e,this._$AP?.(e))}}class G{get tagName(){return this.element.tagName}get _$AU(){return this._$AM._$AU}constructor(e,t,o,r,i){this.type=1,this._$AH=F,this._$AN=void 0,this.element=e,this.name=t,this._$AM=r,this.options=i,o.length>2||""!==o[0]||""!==o[1]?(this._$AH=Array(o.length-1).fill(new String),this.strings=o):this._$AH=F}_$AI(e,t=this,o,r){const i=this.strings;let s=!1;if(void 0===i)e=Y(this,e,t,0),s=!P(e)||e!==this._$AH&&e!==I,s&&(this._$AH=e);else{const r=e;let n,a;for(e=i[0],n=0;n<i.length-1;n++)a=Y(this,r[o+n],t,n),a===I&&(a=this._$AH[n]),s||=!P(a)||a!==this._$AH[n],a===F?e=F:e!==F&&(e+=(a??"")+i[n+1]),this._$AH[n]=a}s&&!r&&this.j(e)}j(e){e===F?this.element.removeAttribute(this.name):this.element.setAttribute(this.name,e??"")}}class Q extends G{constructor(){super(...arguments),this.type=3}j(e){this.element[this.name]=e===F?void 0:e}}class ee extends G{constructor(){super(...arguments),this.type=4}j(e){this.element.toggleAttribute(this.name,!!e&&e!==F)}}class te extends G{constructor(e,t,o,r,i){super(e,t,o,r,i),this.type=5}_$AI(e,t=this){if((e=Y(this,e,t,0)??F)===I)return;const o=this._$AH,r=e===F&&o!==F||e.capture!==o.capture||e.once!==o.once||e.passive!==o.passive,i=e!==F&&(o===F||r);r&&this.element.removeEventListener(this.name,this,o),i&&this.element.addEventListener(this.name,this,e),this._$AH=e}handleEvent(e){"function"==typeof this._$AH?this._$AH.call(this.options?.host??this.element,e):this._$AH.handleEvent(e)}}class oe{constructor(e,t,o){this.element=e,this.type=6,this._$AN=void 0,this._$AM=t,this.options=o}get _$AU(){return this._$AM._$AU}_$AI(e){Y(this,e)}}const re=x.litHtmlPolyfillSupport;re?.(J,Z),(x.litHtmlVersions??=[]).push("3.3.1");const ie=globalThis;class se extends _{constructor(){super(...arguments),this.renderOptions={host:this},this._$Do=void 0}createRenderRoot(){const e=super.createRenderRoot();return this.renderOptions.renderBefore??=e.firstChild,e}update(e){const t=this.render();this.hasUpdated||(this.renderOptions.isConnected=this.isConnected),super.update(e),this._$Do=((e,t,o)=>{const r=o?.renderBefore??t;let i=r._$litPart$;if(void 0===i){const e=o?.renderBefore??null;r._$litPart$=i=new Z(t.insertBefore(M(),e),e,void 0,o??{})}return i._$AI(e),i})(t,this.renderRoot,this.renderOptions)}connectedCallback(){super.connectedCallback(),this._$Do?.setConnected(!0)}disconnectedCallback(){super.disconnectedCallback(),this._$Do?.setConnected(!1)}render(){return I}}se._$litElement$=!0,se.finalized=!0,ie.litElementHydrateSupport?.({LitElement:se});const ne=ie.litElementPolyfillSupport;ne?.({LitElement:se}),(ie.litElementVersions??=[]).push("4.2.1");const ae=e=>(t,o)=>{void 0!==o?o.addInitializer(()=>{customElements.define(e,t)}):customElements.define(e,t)},le={attribute:!0,type:String,converter:v,reflect:!1,hasChanged:y},de=(e=le,t,o)=>{const{kind:r,metadata:i}=o;let s=globalThis.litPropertyMetadata.get(i);if(void 0===s&&globalThis.litPropertyMetadata.set(i,s=/* @__PURE__ */new Map),"setter"===r&&((e=Object.create(e)).wrapped=!0),s.set(o.name,e),"accessor"===r){const{name:r}=o;return{set(o){const i=t.get.call(this);t.set.call(this,o),this.requestUpdate(r,i,e)},init(t){return void 0!==t&&this.C(r,void 0,e,t),t}}}if("setter"===r){const{name:r}=o;return function(o){const i=this[r];t.call(this,o),this.requestUpdate(r,i,e)}}throw Error("Unsupported decorator location: "+r)};function ce(e){return(t,o)=>"object"==typeof o?de(e,t,o):((e,t,o)=>{const r=t.hasOwnProperty(o);return t.constructor.createProperty(o,e),r?Object.getOwnPropertyDescriptor(t,o):void 0})(e,t,o)}function he(e){return ce({...e,state:!0,attribute:!1})}var pe=Object.defineProperty,ue=Object.getOwnPropertyDescriptor,be=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?ue(t,o):t,n=e.length-1;n>=0;n--)(i=e[n])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&pe(t,o,s),s};let ge=class extends se{constructor(){super(...arguments),this.dim="false",this.theme="light"}render(){const e="true"===this.dim,t="dark"===this.theme;let o="whitesmoke",r="0 3px 10px 0 #aaa",i="#1f2937";return t&&e?(o="#1f2937",r="0 3px 10px 0 #000",i="#f3f4f6"):t?(o="#374151",r="0 3px 10px 0 #000",i="#f3f4f6"):e&&(o="silver"),W`
            <div class="box" style="background-color: ${o}; box-shadow: ${r}; color: ${i};">
                <slot @slotchange="${this._handleSlotChange}"></slot>
            </div>`}_handleSlotChange(e){e.target.assignedElements().forEach(e=>{"dark"===this.theme&&e.setAttribute("theme","dark")})}};ge.styles=s`
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
    `,be([ce({type:String})],ge.prototype,"dim",2),be([ce({type:String})],ge.prototype,"theme",2),ge=be([ae("as-box")],ge);var fe=Object.defineProperty,me=Object.getOwnPropertyDescriptor,ve=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?me(t,o):t,n=e.length-1;n>=0;n--)(i=e[n])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&fe(t,o,s),s};let ye=class extends se{constructor(){super(...arguments),this.label="Oops!",this.link="",this.message=""}render(){return W`
      <button @click=${this.onClick}>
        ${this.label}
      </button>`}onClick(){this.link?location.assign(this.link):this.message?this.showNotification(this.message):(this.dispatchEvent(new CustomEvent("button-tap",{bubbles:!0,composed:!0})),console.log("as-button clicked!"))}showNotification(e){const t=document.createElement("div");t.textContent=e,t.style.cssText="position:fixed;bottom:20px;left:50%;transform:translateX(-50%);background:#1f2937;color:white;padding:0.75rem 1.5rem;border-radius:4px;box-shadow:0 4px 6px rgba(0,0,0,0.1);z-index:9999;",document.body.appendChild(t),setTimeout(()=>t.remove(),3500)}};ye.styles=s`
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
    button:active {
      background: #1d4ed8;
    }
  `,ve([ce({type:String})],ye.prototype,"label",2),ve([ce({type:String})],ye.prototype,"link",2),ve([ce({type:String})],ye.prototype,"message",2),ye=ve([ae("as-button")],ye);var $e=Object.defineProperty,_e=Object.getOwnPropertyDescriptor,xe=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?_e(t,o):t,n=e.length-1;n>=0;n--)(i=e[n])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&$e(t,o,s),s};let we=class extends se{constructor(){super(...arguments),this.label="",this.value="",this.placeholder=this.label,this.kind="text",this.theme=""}render(){const e="text"===this.kind?"text":"email"===this.kind?"email":"password"===this.kind?"password":"number"===this.kind?"number":"text";return W`
          <div class="field">
            ${this.label?W`<label>${this.label}</label>`:""}
            <input
              type="${e}"
              .value="${this.value}"
              placeholder="${this.placeholder}"
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
        border: none;
        border-bottom: 1px solid transparent;
        border-radius: 4px;
        font-size: 0.9375rem;
        font-family: inherit;
        background: var(--input-bg, #e8eaed);
        color: var(--text-color, #1a1a1a);
        outline: none;
        transition: border-color 0.15s;
      }
      input:focus {
        border-bottom-color: var(--focus-color, #1676f3);
      }
      input::placeholder {
        color: var(--placeholder-color, #737373);
      }
      :host([theme="dark"]) {
        --label-color: #e5e5e5;
        --border-color: #525252;
        --input-bg: #1f2937;
        --text-color: #e5e5e5;
        --placeholder-color: #737373;
        --focus-color: #1676f3;
      }
    `,xe([ce({type:String})],we.prototype,"label",2),xe([ce({type:String})],we.prototype,"value",2),xe([ce({type:String})],we.prototype,"placeholder",2),xe([ce({type:String})],we.prototype,"kind",2),xe([ce({type:String})],we.prototype,"theme",2),we=xe([ae("as-input")],we);var ke=Object.defineProperty,Se=Object.getOwnPropertyDescriptor,Ae=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?Se(t,o):t,n=e.length-1;n>=0;n--)(i=e[n])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&ke(t,o,s),s};let Ee=class extends se{constructor(){super(...arguments),this.label="",this.checked=!1,this.theme=""}render(){return W`
          <input
            type="checkbox"
            .checked="${this.checked}"
            @change="${e=>{this.checked=e.target.checked,this.dispatchEvent(new CustomEvent("checked-changed",{detail:{value:this.checked},bubbles:!0,composed:!0}))}}"
          />
          ${this.label?W`<label>${this.label}</label>`:""}
        `}};Ee.styles=s`
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
    `,Ae([ce({type:String})],Ee.prototype,"label",2),Ae([ce({type:Boolean})],Ee.prototype,"checked",2),Ae([ce({type:String})],Ee.prototype,"theme",2),Ee=Ae([ae("as-check")],Ee);var Ce=Object.defineProperty,Oe=Object.getOwnPropertyDescriptor,Me=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?Oe(t,o):t,n=e.length-1;n>=0;n--)(i=e[n])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&Ce(t,o,s),s};let Pe=class extends se{constructor(){super(...arguments),this.label="",this.checked=!1,this.theme=""}render(){return W`
          <div
            class="switch ${this.checked?"checked":""}"
            @click="${()=>{this.checked=!this.checked,this.dispatchEvent(new CustomEvent("checked-changed",{detail:{value:this.checked},bubbles:!0,composed:!0}))}}"
          ></div>
          ${this.label?W`<label>${this.label}</label>`:""}
        `}};Pe.styles=s`
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
    `,Me([ce({type:String})],Pe.prototype,"label",2),Me([ce({type:Boolean})],Pe.prototype,"checked",2),Me([ce({type:String})],Pe.prototype,"theme",2),Pe=Me([ae("as-switch")],Pe);var ze,je=Object.defineProperty,De=Object.getOwnPropertyDescriptor,Ue=e=>{throw TypeError(e)},He=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?De(t,o):t,n=e.length-1;n>=0;n--)(i=e[n])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&je(t,o,s),s},Re=(e,t,o)=>t.has(e)||Ue("Cannot "+o);let Te=class extends se{constructor(){var e,t,o;super(...arguments),this.label="Oops!",this.link="",this.message="",e=this,o=!1,(t=ze).has(e)?Ue("Cannot add the same private member more than once"):t instanceof WeakSet?t.add(e):t.set(e,o)}get dialogOpened(){return Re(e=this,t=ze,"read from private field"),o?o.call(e):t.get(e);var e,t,o}set dialogOpened(e){var t,o,r;r=e,Re(t=this,o=ze,"write to private field"),o.set(t,r)}render(){return W`
          <button @click=${this.open}>${this.label}</button>
          ${this.dialogOpened?W`
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
        `}open(){this.dialogOpened=!0}close(){this.dialogOpened=!1}onClick(){console.log("Confirmed!"),this.dialogOpened=!1,this.link&&location.assign(this.link),this.dispatchEvent(new CustomEvent("confirm",{bubbles:!0,composed:!0}))}};ze=/* @__PURE__ */new WeakMap,Te.styles=s`
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
    `,He([ce({type:String})],Te.prototype,"label",2),He([ce({type:String})],Te.prototype,"link",2),He([ce({type:String})],Te.prototype,"message",2),He([he()],Te.prototype,"dialogOpened",1),Te=He([ae("as-confirm")],Te);var Be,Ne,We,Ie=Object.defineProperty,Fe=Object.getOwnPropertyDescriptor,Le=e=>{throw TypeError(e)},Ve=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?Fe(t,o):t,n=e.length-1;n>=0;n--)(i=e[n])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&Ie(t,o,s),s},Ke=(e,t,o)=>t.has(e)||Le("Cannot "+o),qe=(e,t,o)=>(Ke(e,t,"read from private field"),o?o.call(e):t.get(e)),Je=(e,t,o)=>t.has(e)?Le("Cannot add the same private member more than once"):t instanceof WeakSet?t.add(e):t.set(e,o),Ye=(e,t,o,r)=>(Ke(e,t,"write to private field"),t.set(e,o),o);let Xe=class extends se{constructor(){super(...arguments),this.label="",this.value="",this.placeholder=this.label,this.theme="",Je(this,Be,!1),Je(this,Ne,/* @__PURE__ */(new Date).getFullYear()),Je(this,We,/* @__PURE__ */(new Date).getMonth())}get _open(){return qe(this,Be)}set _open(e){Ye(this,Be,e)}get _year(){return qe(this,Ne)}set _year(e){Ye(this,Ne,e)}get _month(){return qe(this,We)}set _month(e){Ye(this,We,e)}render(){const e=this.value||this.placeholder||"YYYY-MM-DD";return W`
        <div class="field">
            ${this.label?W`<label>${this.label}</label>`:""}
            <div
                class="date-trigger"
                tabindex="0"
                @click="${()=>this._open=!this._open}"
                @blur="${()=>setTimeout(()=>this._open=!1,200)}"
            >
                <span>${e}</span>
                <span class="icon">
                    <svg viewBox="0 0 24 24">
                        <path d="M19 3h-1V1h-2v2H8V1H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11z"/>
                    </svg>
                </span>
            </div>
            ${this._open?W`
                <div class="dropdown" @mousedown="${e=>e.preventDefault()}">
                    <div class="header">
                        <button @click="${()=>this._changeMonth(-1)}">‹</button>
                        <div class="month-year">${this._getMonthName()} ${this._year}</div>
                        <button @click="${()=>this._changeMonth(1)}">›</button>
                    </div>
                    <div class="weekdays">
                        ${["S","M","T","W","T","F","S"].map(e=>W`<div class="weekday">${e}</div>`)}
                    </div>
                    <div class="days">
                        ${this._getDays().map(e=>W`
                            <div
                                class="day ${e.selected?"selected":""} ${e.otherMonth?"other-month":""}"
                                @click="${()=>this._selectDay(e.date)}"
                            >
                                ${e.day}
                            </div>
                        `)}
                    </div>
                </div>
            `:""}
        </div>`}_getMonthName(){return["January","February","March","April","May","June","July","August","September","October","November","December"][this._month]}_changeMonth(e){this._month+=e,this._month<0&&(this._month=11,this._year--),this._month>11&&(this._month=0,this._year++)}_getDays(){const e=new Date(this._year,this._month,1).getDay(),t=new Date(this._year,this._month+1,0).getDate(),o=new Date(this._year,this._month,0).getDate(),r=[];for(let s=e-1;s>=0;s--)r.push({day:o-s,otherMonth:!0,date:null});for(let s=1;s<=t;s++){const e=`${this._year}-${String(this._month+1).padStart(2,"0")}-${String(s).padStart(2,"0")}`;r.push({day:s,otherMonth:!1,selected:this.value===e,date:e})}const i=42-r.length;for(let s=1;s<=i;s++)r.push({day:s,otherMonth:!0,date:null});return r}_selectDay(e){e&&(this.value=e,this._open=!1,this.dispatchEvent(new CustomEvent("value-changed",{detail:{value:this.value},bubbles:!0,composed:!0})))}};Be=/* @__PURE__ */new WeakMap,Ne=/* @__PURE__ */new WeakMap,We=/* @__PURE__ */new WeakMap,Xe.styles=s`
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
        border: none;
        border-bottom: 1px solid transparent;
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
        border-bottom-color: var(--focus-color, #1676f3);
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
        --label-color: #e5e5e5;
        --border-color: #525252;
        --input-bg: #1f2937;
        --text-color: #e5e5e5;
        --focus-color: #1676f3;
        --dropdown-bg: #262626;
        --option-hover: #404040;
        --option-selected: #1676f3;
      }
    `,Ve([ce({type:String})],Xe.prototype,"label",2),Ve([ce({type:String})],Xe.prototype,"value",2),Ve([ce({type:String})],Xe.prototype,"placeholder",2),Ve([ce({type:String})],Xe.prototype,"theme",2),Ve([he()],Xe.prototype,"_open",1),Ve([he()],Xe.prototype,"_year",1),Ve([he()],Xe.prototype,"_month",1),Xe=Ve([ae("as-date")],Xe);var Ze,Ge,Qe,et,tt=Object.defineProperty,ot=Object.getOwnPropertyDescriptor,rt=e=>{throw TypeError(e)},it=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?ot(t,o):t,n=e.length-1;n>=0;n--)(i=e[n])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&tt(t,o,s),s},st=(e,t,o)=>t.has(e)||rt("Cannot "+o),nt=(e,t,o)=>(st(e,t,"read from private field"),o?o.call(e):t.get(e)),at=(e,t,o)=>t.has(e)?rt("Cannot add the same private member more than once"):t instanceof WeakSet?t.add(e):t.set(e,o),lt=(e,t,o,r)=>(st(e,t,"write to private field"),t.set(e,o),o);let dt=class extends se{constructor(){super(...arguments),this.label="",this.value="",this.placeholder=this.label,this.theme="",at(this,Ze,!1),at(this,Ge,"12"),at(this,Qe,"00"),at(this,et,"AM")}get _open(){return nt(this,Ze)}set _open(e){lt(this,Ze,e)}get _hour(){return nt(this,Ge)}set _hour(e){lt(this,Ge,e)}get _minute(){return nt(this,Qe)}set _minute(e){lt(this,Qe,e)}get _period(){return nt(this,et)}set _period(e){lt(this,et,e)}render(){const e=this.value||this.placeholder||"HH:MM";return W`
        <div class="field">
            ${this.label?W`<label>${this.label}</label>`:""}
            <div
                class="time-trigger"
                tabindex="0"
                @click="${()=>this._open=!this._open}"
                @blur="${()=>setTimeout(()=>this._open=!1,200)}"
            >
                <span>${e}</span>
                <span class="icon">
                    <svg viewBox="0 0 24 24">
                        <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm.5-13H11v6l5.2 3.2.8-1.3-4.5-2.7V7z"/>
                    </svg>
                </span>
            </div>
            ${this._open?W`
                <div class="dropdown">
                    <div class="time-display">${this._hour}:${this._minute} ${this._period}</div>
                    <div class="selectors">
                        <div class="column">
                            ${Array.from({length:12},(e,t)=>{const o=(t+1).toString().padStart(2,"0");return W`
                                    <div
                                        class="option ${this._hour===o?"selected":""}"
                                        @click="${()=>{this._hour=o,this._updateValue()}}"
                                    >
                                        ${o}
                                    </div>
                                `})}
                        </div>
                        <div class="column">
                            ${Array.from({length:60},(e,t)=>{const o=t.toString().padStart(2,"0");return W`
                                    <div
                                        class="option ${this._minute===o?"selected":""}"
                                        @click="${()=>{this._minute=o,this._updateValue()}}"
                                    >
                                        ${o}
                                    </div>
                                `})}
                        </div>
                        <div class="period-column">
                            <div
                                class="option ${"AM"===this._period?"selected":""}"
                                @click="${()=>{this._period="AM",this._updateValue()}}"
                            >
                                AM
                            </div>
                            <div
                                class="option ${"PM"===this._period?"selected":""}"
                                @click="${()=>{this._period="PM",this._updateValue()}}"
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
        border: none;
        border-bottom: 1px solid transparent;
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
        border-bottom-color: var(--focus-color, #1676f3);
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
        --label-color: #e5e5e5;
        --border-color: #525252;
        --input-bg: #1f2937;
        --text-color: #e5e5e5;
        --focus-color: #1676f3;
        --dropdown-bg: #262626;
        --option-hover: #404040;
        --option-selected: #1e3a5f;
      }
    `,it([ce({type:String})],dt.prototype,"label",2),it([ce({type:String})],dt.prototype,"value",2),it([ce({type:String})],dt.prototype,"placeholder",2),it([ce({type:String})],dt.prototype,"theme",2),it([he()],dt.prototype,"_open",1),it([he()],dt.prototype,"_hour",1),it([he()],dt.prototype,"_minute",1),it([he()],dt.prototype,"_period",1),dt=it([ae("as-time")],dt);var ct=Object.defineProperty,ht=Object.getOwnPropertyDescriptor,pt=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?ht(t,o):t,n=e.length-1;n>=0;n--)(i=e[n])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&ct(t,o,s),s};let ut=class extends se{constructor(){super(),this.width=1200,this.height=675,this.url="",this.width=1200,this.height=675,this.url=""}render(){return W`
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
    }`,ut.properties={width:{type:Number},height:{type:Number},url:{type:String}},pt([ce({type:String})],ut.prototype,"width",2),pt([ce({type:String})],ut.prototype,"height",2),pt([ce({type:String})],ut.prototype,"url",2),ut=pt([ae("as-embed")],ut);var bt=Object.defineProperty,gt=Object.getOwnPropertyDescriptor,ft=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?gt(t,o):t,n=e.length-1;n>=0;n--)(i=e[n])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&bt(t,o,s),s};let mt=class extends se{constructor(){super(...arguments),this.url=""}render(){return W`
        <div class="image-container">
          <br />
          <img src="${this.url}" />
          <br />
        </div>`}};mt.styles=s`
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
    }`,ft([ce({type:String})],mt.prototype,"url",2),mt=ft([ae("as-image")],mt);class vt{planeDeserialize(e){const t=e.split(";"),o=[];try{""!==e&&"[]"!==e&&t.forEach(e=>{const t=e.split(","),r={};t.forEach(e=>{const[t,o]=e.split("=");r[t]=o}),o.push(r)})}catch(r){console.log("planeDeserialize => IndexOutOfBounds! input =",e)}return o}}var yt=Object.defineProperty,$t=Object.getOwnPropertyDescriptor,_t=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?$t(t,o):t,n=e.length-1;n>=0;n--)(i=e[n])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&yt(t,o,s),s};let xt=class extends se{constructor(){super(...arguments),this.label="",this.value="",this.options="label=A,value=A;label=B,value=B;label=C,value=C",this.theme=""}get items(){return(new vt).planeDeserialize(this.options).map(e=>({label:e.label,value:e.value}))}render(){return W`
        <div class="group">
            ${this.label?W`<div class="group-label">${this.label}</div>`:""}
            <div class="options">
                ${this.items.map(e=>W`
                    <label class="option">
                        <input
                            type="radio"
                            name="radio-group"
                            .value="${e.value}"
                            ?checked="${this.value===e.value}"
                            @change="${e=>{this.value=e.target.value,this.dispatchEvent(new CustomEvent("value-changed",{detail:{value:this.value},bubbles:!0,composed:!0}))}}"
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
        --label-color: #e5e5e5;
        --text-color: #e5e5e5;
        --accent-color: #1676f3;
      }
    `,_t([ce({type:String})],xt.prototype,"label",2),_t([ce({type:String})],xt.prototype,"value",2),_t([ce({type:String})],xt.prototype,"options",2),_t([ce({type:String})],xt.prototype,"theme",2),_t([ce({type:Array})],xt.prototype,"items",1),xt=_t([ae("as-radio")],xt);var wt,kt=Object.defineProperty,St=Object.getOwnPropertyDescriptor,At=e=>{throw TypeError(e)},Et=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?St(t,o):t,n=e.length-1;n>=0;n--)(i=e[n])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&kt(t,o,s),s},Ct=(e,t,o)=>t.has(e)||At("Cannot "+o);let Ot=class extends se{constructor(){var e,t,o;super(...arguments),this.label="",this.value="",this.options="label=A,value=A;label=B,value=B;label=C,value=C",this.theme="",e=this,o=!1,(t=wt).has(e)?At("Cannot add the same private member more than once"):t instanceof WeakSet?t.add(e):t.set(e,o)}get _open(){return Ct(e=this,t=wt,"read from private field"),o?o.call(e):t.get(e);var e,t,o}set _open(e){var t,o,r;r=e,Ct(t=this,o=wt,"write to private field"),o.set(t,r)}get items(){return(new vt).planeDeserialize(this.options).map(e=>({label:e.label,value:e.value}))}render(){const e=this.items.find(e=>e.value===this.value)||this.items[0];return W`
        <div class="field">
            ${this.label?W`<label>${this.label}</label>`:""}
            <div
                class="select-trigger"
                tabindex="0"
                @click="${()=>this._open=!this._open}"
                @blur="${()=>setTimeout(()=>this._open=!1,200)}"
            >
                <span>${e?.label||""}</span>
                <span class="arrow ${this._open?"open":""}">
                    <svg viewBox="0 0 24 24">
                        <path d="M7 10l5 5 5-5" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </span>
            </div>
            ${this._open?W`
                <div class="dropdown">
                    ${this.items.map(e=>W`
                        <div
                            class="option ${this.value===e.value?"selected":""}"
                            @click="${()=>{this.value=e.value,this._open=!1,this.dispatchEvent(new CustomEvent("value-changed",{detail:{value:this.value},bubbles:!0,composed:!0}))}}"
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
        border: none;
        border-bottom: 1px solid transparent;
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
        border-bottom-color: var(--focus-color, #1676f3);
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
        --label-color: #e5e5e5;
        --border-color: #525252;
        --input-bg: #1f2937;
        --text-color: #e5e5e5;
        --focus-color: #1676f3;
        --dropdown-bg: #262626;
        --option-hover: #404040;
        --option-selected: #1e3a5f;
      }
    `,Et([ce({type:String})],Ot.prototype,"label",2),Et([ce({type:String})],Ot.prototype,"value",2),Et([ce({type:String})],Ot.prototype,"options",2),Et([ce({type:String})],Ot.prototype,"theme",2),Et([he()],Ot.prototype,"_open",1),Et([ce({type:Array})],Ot.prototype,"items",1),Ot=Et([ae("as-select")],Ot);var Mt=Object.defineProperty,Pt=Object.getOwnPropertyDescriptor,zt=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?Pt(t,o):t,n=e.length-1;n>=0;n--)(i=e[n])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&Mt(t,o,s),s};let jt=class extends se{constructor(){super(...arguments),this.label="",this.value="",this.placeholder=this.label,this.rows=3,this.theme=""}render(){return W`
        <div class="field">
            ${this.label?W`<label>${this.label}</label>`:""}
            <textarea
                rows="${this.rows}"
                placeholder="${this.placeholder}"
                .value="${this.value}"
                @input="${e=>{this.value=e.target.value,this.dispatchEvent(new CustomEvent("value-changed",{detail:{value:this.value},bubbles:!0,composed:!0}))}}"
            ></textarea>
        </div>
        `}};jt.styles=s`
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
        border: none;
        border-bottom: 1px solid transparent;
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
        border-bottom-color: var(--focus-color, #1676f3);
      }
      textarea::placeholder {
        color: var(--placeholder-color, #737373);
      }
      :host([theme="dark"]) {
        --label-color: #e5e5e5;
        --border-color: #525252;
        --input-bg: #1f2937;
        --text-color: #e5e5e5;
        --placeholder-color: #737373;
        --focus-color: #1676f3;
      }
    `,zt([ce({type:String})],jt.prototype,"label",2),zt([ce({type:String})],jt.prototype,"value",2),zt([ce({type:String})],jt.prototype,"placeholder",2),zt([ce({type:Number})],jt.prototype,"rows",2),zt([ce({type:String})],jt.prototype,"theme",2),jt=zt([ae("as-text")],jt);var Dt=Object.defineProperty,Ut=Object.getOwnPropertyDescriptor,Ht=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?Ut(t,o):t,n=e.length-1;n>=0;n--)(i=e[n])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&Dt(t,o,s),s};let Rt=class extends se{constructor(){super(),this.width=560,this.height=315,this.url="",this.width=560,this.height=315,this.url=""}updated(e){e.has("width")&&window.innerWidth<560&&(this.width=310,this.height=175)}render(){return W`
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
        </div>`}};Rt.styles=s`
    .video {
      display: grid;
      grid-template-areas: stack;
      place-items: center;
      width: max(320px, 100%);
    }`,Ht([ce({type:String})],Rt.prototype,"width",2),Ht([ce({type:String})],Rt.prototype,"height",2),Ht([ce({type:String})],Rt.prototype,"url",2),Rt=Ht([ae("as-video")],Rt);var Tt,Bt,Nt,Wt,It,Ft=Object.defineProperty,Lt=Object.getOwnPropertyDescriptor,Vt=e=>{throw TypeError(e)},Kt=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?Lt(t,o):t,n=e.length-1;n>=0;n--)(i=e[n])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&Ft(t,o,s),s},qt=(e,t,o)=>t.has(e)||Vt("Cannot "+o),Jt=(e,t,o)=>(qt(e,t,"read from private field"),o?o.call(e):t.get(e)),Yt=(e,t,o)=>t.has(e)?Vt("Cannot add the same private member more than once"):t instanceof WeakSet?t.add(e):t.set(e,o),Xt=(e,t,o,r)=>(qt(e,t,"write to private field"),t.set(e,o),o);let Zt=class extends se{constructor(){super(...arguments),this.data=[],this.columns=[],this.pageSize=15,this.title="",this.theme="",this.selectable=!1,this.pageable=!1,this.filterable=!1,Yt(this,Tt,""),Yt(this,Bt,null),Yt(this,Nt,1),Yt(this,Wt,0),Yt(this,It,null)}get _filter(){return Jt(this,Tt)}set _filter(e){Xt(this,Tt,e)}get _sortKey(){return Jt(this,Bt)}set _sortKey(e){Xt(this,Bt,e)}get _sortDir(){return Jt(this,Nt)}set _sortDir(e){Xt(this,Nt,e)}get _page(){return Jt(this,Wt)}set _page(e){Xt(this,Wt,e)}get _selectedRow(){return Jt(this,It)}set _selectedRow(e){Xt(this,It,e)}_getFilteredData(){if(!this._filter)return this.data;const e=this._filter.toLowerCase();return this.data.filter(t=>Object.values(t).some(t=>String(t).toLowerCase().includes(e)))}_getSortedData(){const e=this._getFilteredData();return this._sortKey?[...e].sort((e,t)=>{const o=e[this._sortKey],r=t[this._sortKey];return o<r?-this._sortDir:o>r?this._sortDir:0}):e}_getPaginatedData(){const e=this._getSortedData();if(!this.pageable)return e;const t=this._page*this.pageSize;return e.slice(t,t+this.pageSize)}_sort(e){this._sortKey===e?this._sortDir=1===this._sortDir?-1:1:(this._sortKey=e,this._sortDir=1)}_selectRow(e){this.selectable&&(this._selectedRow=e,this.dispatchEvent(new CustomEvent("row-select",{detail:{row:e,id:e.id},bubbles:!0,composed:!0})))}render(){if(!this.data.length||!this.columns.length)return W``;const e=this._getPaginatedData(),t=this._getSortedData().length,o=Math.ceil(t/this.pageSize);return W`
      <div class="container">
        ${this.title||this.filterable?W`
          <div class="header">
            ${this.title?W`<div class="title">${this.title}</div>`:W`<div></div>`}
            ${this.filterable?W`
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
              ${this.selectable?W`<th class="select-col"></th>`:""}
              ${this.columns.map(e=>W`
                <th @click=${()=>this._sort(e.key)}>
                  ${e.header}
                  ${this._sortKey===e.key?1===this._sortDir?" ↑":" ↓":""}
                </th>
              `)}
            </tr>
          </thead>
          <tbody>
            ${e.map(e=>W`
              <tr 
                class="${this.selectable?"selectable":""} ${this._selectedRow===e?"selected":""}"
                @click=${()=>this._selectRow(e)}
              >
                ${this.selectable?W`<td class="select-col">${this._selectedRow===e?"»":""}</td>`:""}
                ${this.columns.map(t=>W`<td>${e[t.key]}</td>`)}
              </tr>
            `)}
          </tbody>
          </table>
        </div>

        ${this.pageable?W`
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
    `}};Tt=/* @__PURE__ */new WeakMap,Bt=/* @__PURE__ */new WeakMap,Nt=/* @__PURE__ */new WeakMap,Wt=/* @__PURE__ */new WeakMap,It=/* @__PURE__ */new WeakMap,Zt.styles=s`
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
      --input-bg: white;
      --input-border: #d1d5db;
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
      --input-bg: #374151;
      --input-border: #4b5563;
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
      --input-bg: white;
      --input-border: #d1d5db;
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
      padding: 0.5rem 0.5rem;
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
    th.select-col {
      width: 1ch;
      padding: 0.5rem 0;
      cursor: default;
    }
    tbody tr {
      border-bottom: 1px solid var(--table-border);
      transition: background-color 0.15s;
    }
    tbody tr:nth-child(even) {
      background-color: var(--table-row-even);
    }

    td {
      padding: 0.5rem 0.5rem;
      font-size: 0.9375rem;
      color: var(--table-text);
    }
    td.select-col {
      width: 1ch;
      padding: 0.5rem 0;
      text-align: center;
      font-size: 0.75rem;
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
      max-width: 400px;
      padding: 0.5rem 0.75rem;
      border: 1px solid var(--table-border-strong);
      border-radius: 4px;
      font-size: 0.9375rem;
      font-family: inherit;
      background: var(--input-bg, white);
      color: var(--table-text);
      outline: none;
    }
    .filter-input:focus {
      border-color: var(--focus-color, #3b82f6);
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
  `,Kt([ce({type:Array})],Zt.prototype,"data",2),Kt([ce({type:Array})],Zt.prototype,"columns",2),Kt([ce({type:Number})],Zt.prototype,"pageSize",2),Kt([ce({type:String})],Zt.prototype,"title",2),Kt([ce({type:String})],Zt.prototype,"theme",2),Kt([ce({type:Boolean})],Zt.prototype,"selectable",2),Kt([ce({type:Boolean})],Zt.prototype,"pageable",2),Kt([ce({type:Boolean})],Zt.prototype,"filterable",2),Kt([he()],Zt.prototype,"_filter",1),Kt([he()],Zt.prototype,"_sortKey",1),Kt([he()],Zt.prototype,"_sortDir",1),Kt([he()],Zt.prototype,"_page",1),Kt([he()],Zt.prototype,"_selectedRow",1),Zt=Kt([ae("as-datagrid")],Zt);var Gt,Qt,eo=Object.defineProperty,to=Object.getOwnPropertyDescriptor,oo=e=>{throw TypeError(e)},ro=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?to(t,o):t,n=e.length-1;n>=0;n--)(i=e[n])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&eo(t,o,s),s},io=(e,t,o)=>t.has(e)||oo("Cannot "+o),so=(e,t,o)=>(io(e,t,"read from private field"),o?o.call(e):t.get(e)),no=(e,t,o)=>t.has(e)?oo("Cannot add the same private member more than once"):t instanceof WeakSet?t.add(e):t.set(e,o),ao=(e,t,o,r)=>(io(e,t,"write to private field"),t.set(e,o),o);let lo=class extends se{constructor(){super(...arguments),this.label="",this.value="",this.options="label=A,value=A;label=B,value=B;label=C,value=C",this.theme="",no(this,Gt,""),no(this,Qt,!1)}get _filter(){return so(this,Gt)}set _filter(e){ao(this,Gt,e)}get _open(){return so(this,Qt)}set _open(e){ao(this,Qt,e)}get items(){return(new vt).planeDeserialize(this.options).map(e=>({label:e.label,value:e.value}))}render(){const e=this._filter?this.items.filter(e=>e.label.toLowerCase().includes(this._filter.toLowerCase())):this.items;return W`
        <div class="field">
            ${this.label?W`<label>${this.label}</label>`:""}
            <input
                type="text"
                .value="${this._filter}"
                placeholder="${this.label||"Buscar..."}"
                @input="${e=>{this._filter=e.target.value,this._open=!0}}"
                @focus="${()=>this._open=!0}"
                @blur="${()=>setTimeout(()=>this._open=!1,200)}"
            />
            ${this._open&&e.length?W`
                <div class="dropdown">
                    ${e.map(e=>W`
                        <div class="option" @click="${()=>{this.value=e.value,this._filter=e.label,this._open=!1,this.dispatchEvent(new CustomEvent("value-changed",{detail:{value:this.value},bubbles:!0,composed:!0}))}}">
                            ${e.label}
                        </div>
                    `)}
                </div>
            `:""}
        </div>
        `}};Gt=/* @__PURE__ */new WeakMap,Qt=/* @__PURE__ */new WeakMap,lo.styles=s`
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
        border: none;
        border-bottom: 1px solid transparent;
        border-radius: 4px;
        font-size: 0.9375rem;
        font-family: inherit;
        background: var(--input-bg, #e8eaed);
        color: var(--text-color, #1a1a1a);
        outline: none;
        transition: border-color 0.15s;
      }
      input:focus {
        border-bottom-color: var(--focus-color, #1676f3);
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
        --label-color: #e5e5e5;
        --border-color: #525252;
        --input-bg: #1f2937;
        --text-color: #e5e5e5;
        --focus-color: #1676f3;
        --dropdown-bg: #262626;
        --option-hover: #404040;
      }
    `,ro([ce({type:String})],lo.prototype,"label",2),ro([ce({type:String})],lo.prototype,"value",2),ro([ce({type:String})],lo.prototype,"options",2),ro([ce({type:String})],lo.prototype,"theme",2),ro([he()],lo.prototype,"_filter",1),ro([he()],lo.prototype,"_open",1),ro([ce({type:Array})],lo.prototype,"items",1),lo=ro([ae("as-complete")],lo);var co=Object.defineProperty,ho=Object.getOwnPropertyDescriptor,po=(e,t,o,r)=>{for(var i,s=r>1?void 0:r?ho(t,o):t,n=e.length-1;n>=0;n--)(i=e[n])&&(s=(r?i(t,o,s):i(s))||s);return r&&s&&co(t,o,s),s};let uo=class extends se{constructor(){super(...arguments),this.label="",this.value="",this.placeholder="",this.event="event-trigger",this.theme=""}render(){const e=this.value||this.placeholder||"";return W`
        <div class="field">
            ${this.label?W`<label>${this.label}</label>`:""}
            <div
                class="event-trigger"
                tabindex="0"
                @click="${this._handleClick}"
            >
                <span>${e}</span>
                <span class="arrow">
                    <svg viewBox="0 0 24 24">
                        <path d="M7 10l5 5 5-5" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                </span>
            </div>
        </div>
        `}_handleClick(){this.dispatchEvent(new CustomEvent(this.event,{detail:{value:this.value},bubbles:!0,composed:!0}))}};uo.styles=s`
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
        border: none;
        border-bottom: 1px solid transparent;
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
        border-bottom-color: var(--focus-color, #1676f3);
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
        --label-color: #e5e5e5;
        --border-color: #525252;
        --input-bg: #1f2937;
        --text-color: #e5e5e5;
        --focus-color: #1676f3;
      }
    `,po([ce({type:String})],uo.prototype,"label",2),po([ce({type:String})],uo.prototype,"value",2),po([ce({type:String})],uo.prototype,"placeholder",2),po([ce({type:String})],uo.prototype,"event",2),po([ce({type:String})],uo.prototype,"theme",2),uo=po([ae("as-event")],uo);export{ge as AsBox,ye as AsButton,Ee as AsCheck,lo as AsComplete,Te as AsConfirm,Zt as AsDatagrid,Xe as AsDate,ut as AsEmbed,uo as AsEvent,mt as AsImage,we as AsInput,xt as AsRadio,Ot as AsSelect,Pe as AsSwitch,jt as AsText,dt as AsTime,Rt as AsVideo};
