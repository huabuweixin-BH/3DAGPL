<template>
<div style="padding:24px">

<div class="controls">
<input type="file" @change="onFileChange" accept=".obj" />

<span v-if="vertexCount">
原始顶点: {{originalCount}} | 当前顶点: {{vertexCount}}
</span>

<input v-model.number="targetCount" type="number" style="width:100px"/>

<button @click="executeSimplify" :disabled="simplifying">
{{simplifying?'简化中...':'执行简化'}}
</button>
</div>

<div ref="threeContainer" class="canvas-container"></div>

</div>
</template>

<script>

import * as THREE from "three"
import {OBJLoader} from "three/examples/jsm/loaders/OBJLoader.js"
import {OrbitControls} from "three/examples/jsm/controls/OrbitControls.js"
import * as BufferGeometryUtils from "three/examples/jsm/utils/BufferGeometryUtils.js"

/* ---------------- HalfEdge ---------------- */

class HalfEdge{
constructor(){
this.vertex=null
this.next=null
this.prev=null
this.twin=null
this.face=null
}
}

class Vertex{
constructor(pos){
this.position=pos.clone()
this.halfEdge=null
this.Q=new THREE.Matrix4().set(
0,0,0,0,
0,0,0,0,
0,0,0,0,
0,0,0,0
)
this.id=-1
}
}

class Face{
constructor(){
this.halfEdge=null
this.normal=new THREE.Vector3()
}
}

/* ---------------- HalfEdgeMesh ---------------- */

class HalfEdgeMesh{

constructor(){
this.vertices=[]
this.faces=[]
this.halfEdges=[]
}

/* build mesh */

buildFromGeometry(geometry){

const pos=geometry.attributes.position.array
const idx=geometry.index.array

for(let i=0;i<pos.length;i+=3){
const v=new Vertex(new THREE.Vector3(pos[i],pos[i+1],pos[i+2]))
v.id=this.vertices.length
this.vertices.push(v)
}

const edgeMap=new Map()

for(let i=0;i<idx.length;i+=3){

const a=idx[i]
const b=idx[i+1]
const c=idx[i+2]

const he0=new HalfEdge()
const he1=new HalfEdge()
const he2=new HalfEdge()

const face=new Face()

he0.vertex=this.vertices[b]
he1.vertex=this.vertices[c]
he2.vertex=this.vertices[a]

he0.next=he1
he1.next=he2
he2.next=he0

he0.prev=he2
he1.prev=he0
he2.prev=he1

he0.face=he1.face=he2.face=face
face.halfEdge=he0

this.halfEdges.push(he0,he1,he2)
this.faces.push(face)

this.linkTwin(edgeMap,a,b,he0)
this.linkTwin(edgeMap,b,c,he1)
this.linkTwin(edgeMap,c,a,he2)

if(!this.vertices[a].halfEdge) this.vertices[a].halfEdge=he0
if(!this.vertices[b].halfEdge) this.vertices[b].halfEdge=he1
if(!this.vertices[c].halfEdge) this.vertices[c].halfEdge=he2

this.updateFaceNormal(face)
}

this.computeQuadrics()
}

linkTwin(map,a,b,he){

const key=a+"_"+b
const twin=b+"_"+a

if(map.has(twin)){

const t=map.get(twin)
he.twin=t
t.twin=he

}else{

map.set(key,he)

}
}

updateFaceNormal(face){

const he=face.halfEdge

const v0=he.prev.vertex.position
const v1=he.vertex.position
const v2=he.next.vertex.position

face.normal.crossVectors(
new THREE.Vector3().subVectors(v1,v0),
new THREE.Vector3().subVectors(v2,v0)
).normalize()

}

/* ---------------- Quadric ---------------- */

computeQuadrics(){

for(const face of this.faces){

const he=face.halfEdge

const v0=he.prev.vertex.position
const v1=he.vertex.position
const v2=he.next.vertex.position

const n=new THREE.Vector3()

n.crossVectors(
new THREE.Vector3().subVectors(v1,v0),
new THREE.Vector3().subVectors(v2,v0)
).normalize()

const d=-n.dot(v0)

const p=[n.x,n.y,n.z,d]

const K=new THREE.Matrix4()

const e=[]

for(let i=0;i<4;i++)
for(let j=0;j<4;j++)
e[i*4+j]=p[i]*p[j]

K.set(
e[0],e[1],e[2],e[3],
e[4],e[5],e[6],e[7],
e[8],e[9],e[10],e[11],
e[12],e[13],e[14],e[15]
)

let h=he

for(let i=0;i<3;i++){
this.addMatrix(h.prev.vertex.Q,K)
h=h.next
}

}
}

addMatrix(a,b){

for(let i=0;i<16;i++)
a.elements[i]+=b.elements[i]

}

/* ---------------- Edge Collapse ---------------- */

collapseEdge(he,target){

const v1=he.prev.vertex
const v2=he.vertex

v1.position.copy(target)

this.addMatrix(v1.Q,v2.Q)

for(const edge of this.halfEdges){
if(edge.vertex===v2)
edge.vertex=v1
}

v2.halfEdge=null

this.removeFace(he.face)

if(he.twin)
this.removeFace(he.twin.face)
}

removeFace(face){

if(!face) return

let he=face.halfEdge

for(let i=0;i<3;i++){

const id=this.halfEdges.indexOf(he)

if(id!=-1)
this.halfEdges.splice(id,1)

he=he.next

}

const fi=this.faces.indexOf(face)

if(fi!=-1)
this.faces.splice(fi,1)

}

/* ---------------- neighbor ---------------- */

getNeighborVertices(v){

const list=[]

let he=v.halfEdge
if(!he) return list

const start=he

do{

list.push(he.vertex)

he=he.twin?.next

}while(he && he!==start)

return list

}

getEdges(){

const edges=[]
const set=new Set()

for(const he of this.halfEdges){

const a=he.prev.vertex.id
const b=he.vertex.id

const key=a<b?a+"_"+b:b+"_"+a

if(!set.has(key)){
set.add(key)
edges.push(he)
}

}

return edges

}

getVertexCount(){
return this.vertices.filter(v=>v.halfEdge!=null).length
}

}

/* ---------------- MinHeap ---------------- */

class MinHeap{

constructor(compare){
this.nodes=[]
this.compare=compare
}

push(n){
this.nodes.push(n)
this.bubble(this.nodes.length-1)
}

bubble(i){

while(i>0){

const p=(i-1)>>1

if(this.compare(this.nodes[i],this.nodes[p])<0){

[this.nodes[i],this.nodes[p]]=[this.nodes[p],this.nodes[i]]

i=p

}else break

}

}

pop(){

if(this.nodes.length===0) return null

const top=this.nodes[0]
const end=this.nodes.pop()

if(this.nodes.length>0){
this.nodes[0]=end
this.sink(0)
}

return top
}

sink(i){

while(true){

let l=i*2+1
let r=i*2+2
let s=i

if(l<this.nodes.length && this.compare(this.nodes[l],this.nodes[s])<0) s=l
if(r<this.nodes.length && this.compare(this.nodes[r],this.nodes[s])<0) s=r

if(s!==i){
[this.nodes[i],this.nodes[s]]=[this.nodes[s],this.nodes[i]]
i=s
}else break

}

}

size(){return this.nodes.length}

}

/* ---------------- Vue ---------------- */

export default{

data(){
return{

scene:null,
camera:null,
renderer:null,

objMesh:null,

originalCount:0,
vertexCount:null,

targetCount:500,

simplifying:false
}
},

methods:{

executeSimplify(){

if(!this.objMesh) return

const mesh=this.findFirstMesh()

let geometry=mesh.geometry.clone()

geometry=BufferGeometryUtils.mergeVertices(geometry,0.001)

const heMesh=new HalfEdgeMesh()

heMesh.buildFromGeometry(geometry)

const heap=new MinHeap((a,b)=>a.cost-b.cost)

const edges=heMesh.getEdges()

for(const he of edges){

if(!he.twin) continue

const cost=this.calculateEdgeCost(he,heMesh)

heap.push({halfEdge:he,cost:cost.cost,targetPos:cost.targetPos})

}

while(heMesh.getVertexCount()>this.targetCount && heap.size()>0){

const e=heap.pop()

if(!e.halfEdge.prev.vertex || !e.halfEdge.vertex) continue

heMesh.collapseEdge(e.halfEdge,e.targetPos)

}

this.updateMesh(mesh,heMesh)

this.vertexCount=heMesh.getVertexCount()

},

calculateEdgeCost(he,mesh){

const v1=he.prev.vertex
const v2=he.vertex

const Q=new THREE.Matrix4().copy(v1.Q)

mesh.addMatrix(Q,v2.Q)

const target=new THREE.Vector3()
.addVectors(v1.position,v2.position)
.multiplyScalar(0.5)

const v=new THREE.Vector4(target.x,target.y,target.z,1)

const r=v.clone().applyMatrix4(Q)

const cost=v.x*r.x+v.y*r.y+v.z*r.z+v.w*r.w

return{cost,targetPos:target}

},

updateMesh(mesh,heMesh){

const pos=[]
const idx=[]
const map=new Map()

let id=0

for(const v of heMesh.vertices){

if(v.halfEdge){

map.set(v.id,id++)

pos.push(v.position.x,v.position.y,v.position.z)

}

}

for(const f of heMesh.faces){

let he=f.halfEdge

const a=map.get(he.prev.vertex.id)
const b=map.get(he.vertex.id)
const c=map.get(he.next.vertex.id)

if(a!=b && b!=c && a!=c)
idx.push(a,b,c)

}

const geo=new THREE.BufferGeometry()

geo.setAttribute("position",new THREE.Float32BufferAttribute(pos,3))
geo.setIndex(idx)
geo.computeVertexNormals()

mesh.geometry.dispose()

mesh.geometry=geo

},

initThree(){

const container=this.$refs.threeContainer

this.scene=new THREE.Scene()

this.camera=new THREE.PerspectiveCamera(
45,
container.clientWidth/container.clientHeight,
0.1,
1000
)

this.camera.position.set(2,2,5)

this.renderer=new THREE.WebGLRenderer({antialias:true})

this.renderer.setSize(container.clientWidth,container.clientHeight)

container.appendChild(this.renderer.domElement)

const light=new THREE.DirectionalLight(0xffffff,1)

light.position.set(5,5,5)

this.scene.add(light)

this.scene.add(new THREE.AmbientLight(0xffffff,0.6))

new OrbitControls(this.camera,this.renderer.domElement)

const loop=()=>{
requestAnimationFrame(loop)
this.renderer.render(this.scene,this.camera)
}

loop()

},

onFileChange(e){

const file=e.target.files[0]

const reader=new FileReader()

reader.onload=(ev)=>{

const obj=new OBJLoader().parse(ev.target.result)

obj.traverse(c=>{
if(c.isMesh){
c.material=new THREE.MeshStandardMaterial({
color:0x44aaee,
wireframe:true
})
this.originalCount=c.geometry.attributes.position.count
this.vertexCount=this.originalCount
}
})

this.objMesh=obj
this.scene.add(obj)

}

reader.readAsText(file)

},

findFirstMesh(){

let m=null

this.objMesh.traverse(c=>{
if(c.isMesh && !m) m=c
})

return m

}

},

mounted(){
this.initThree()
}

}
</script>

<style scoped>

.controls{
margin-bottom:10px;
display:flex;
gap:10px;
align-items:center;
}

.canvas-container{
width:100%;
height:calc(100vh - 120px);
border:1px solid #444;
}

button{
padding:6px 12px;
background:#44aaee;
color:white;
border:none;
cursor:pointer;
}

</style>