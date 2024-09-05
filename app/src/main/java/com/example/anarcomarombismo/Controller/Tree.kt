package com.example.anarcomarombismo.Controller

import android.content.Context
import com.example.anarcomarombismo.R

class Tree(val obj: Any = 0) {  // Changed name to obj and type to Any
    private var value: Int
        get() = values[obj] ?: 0  // Updated to use obj
        set(value) {
            values[obj] = value  // Updated to use obj
        }

    private var superNode: Tree?
        get() = superNodes[obj]
        set(value) {
            superNodes[obj] = value
        }

    companion object {
        private val leafs = mutableSetOf<Tree>()
        private val subNodes = mutableMapOf<Any, MutableSet<Tree>>()  // Updated to use Any
        private val superNodes = mutableMapOf<Any, Tree?>()  // Updated to use Any
        private var values = mutableMapOf<Any, Int>()  // Updated to use Any
        private var isLeaf = mutableMapOf<Any, Boolean>()  // Updated to use Any
    }

    init {
        if (obj.toString().isNotEmpty()) {
            leafs.add(this)
            isLeaf[obj] = true
        } else {
            resetTree()
        }
    }

    private fun setValueInternal(value: Int) {
        values[obj] = value  // Updated to use obj
    }

    private fun resetTree() {
        leafs.clear()
        subNodes.clear()
        superNodes.clear()
        values.clear()
        isLeaf.clear()
    }

    fun addNode(subTree: Tree) {
        if (obj.toString().isNotEmpty()) {
            subTree.superNode = this
            if (!subNodes.containsKey(this.obj)) {
                subNodes[this.obj] = mutableSetOf()
            }
            subNodes[this.obj]!!.add(subTree)
            isLeaf[this.obj] = false
            leafs.remove(this)
        }
    }

    private fun getLeafs(): Set<Tree> {
        return leafs
    }

    private fun sumChildren() {
        var sum = 0
        subNodes[obj]?.forEach { child -> sum += child.value }
        values[obj] = sum
    }

    private fun sumNodes(nodeList: Set<Tree>) {
        val newNodeList = mutableSetOf<Tree>()
        nodeList.forEach { node ->
            node.superNode?.let { superNode ->
                if (superNode !in newNodeList) {
                    superNode.sumChildren()
                    newNodeList.add(superNode)
                }
            }
        }
        if (newNodeList.isNotEmpty()) {
            sumNodes(newNodeList)
        }
    }

    private fun sumAllNodes() {
        sumNodes(leafs)
    }

    fun searchTree(nodeName: Any): Tree? {  // Updated to use Any
        val visitedNodes = mutableSetOf<Tree>()
        fun searchNodes(nodeList: Set<Tree>): Tree? {
            val newNodeList = mutableSetOf<Tree>()
            nodeList.forEach { node ->
                if (node.obj == nodeName) {  // Updated to use obj
                    return node
                }
                node.superNode?.let { superNode ->
                    if (superNode !in newNodeList && superNode !in visitedNodes) {
                        newNodeList.add(superNode)
                    }
                }
                visitedNodes.add(node)
            }
            return if (newNodeList.isNotEmpty()) {
                searchNodes(newNodeList)
            } else {
                null
            }
        }
        return searchNodes(leafs)
    }

    override fun toString(): String {
        var currentNode: Tree? = this
        var result = ""
        while (currentNode != null) {
            result = "-> ${currentNode.obj}:${currentNode.value} $result"  // Updated to use obj
            currentNode = currentNode.superNode
        }
        return result.trimStart('-').trim() + "\n"
    }

    fun toString(context: Context): String {
        var currentNode: Tree? = this
        var result = ""
        while (currentNode != null) {
            result = "-> ${context.getString(currentNode.obj as Int)}:${currentNode.value} $result"  // Updated to use obj
            currentNode = currentNode.superNode
        }
        return result.trimStart('-').trim() + "\n"
    }

    fun dumpAndLoadMuscles(context: Context): Set<Tree> {
        /*
            Musclesthis,:20
            ├── Upper Limbs:6
            │   ├── Triceps:1
            │   ├── Chest:1
            │   └── Deltoids:3
            │       ├── Anterior Deltoids:1
            │       ├── Lateral Deltoids:1
            │       └── Posterior Deltoids:1
            ├── Trunk:9
            │   ├── Abdominals:4
            │   │   ├── Rectus Abdominis:1
            │   │   ├── External Obliques:1
            │   │   ├── Internal Obliques:1
            │   │   └── Transverse Abdominis:1
            │   ├── Back:1
            │   │   ├── Trapezius:1
            │   │   ├── Rhomboids:1
            │   │   └── Erector Spinae:1
            │   └── Serratus Anterior:1
            └── Lower Limbs:5
                ├── Thighs:3
                │   ├── Quadriceps:1
                │   ├── Adductors:1
                │   └── Hamstrings:1
                ├── Glutes:1
                └── Calves:1
        */
        val leafs = Tree("").getLeafs()
        val musculos = Tree(R.string.muscles)
        val membrosSuperiores = Tree(R.string.upper_limbs).also { musculos.addNode(it) }
        val tronco = Tree(R.string.torso).also { musculos.addNode(it) }
        val membrosInferiores = Tree(R.string.lower_members).also { musculos.addNode(it) }
        Tree(R.string.biceps).also { membrosSuperiores.addNode(it) }
        Tree(R.string.triceps).also { membrosSuperiores.addNode(it) }
        Tree(R.string.breastplate).also { membrosSuperiores.addNode(it) }
        val deltoides = Tree(R.string.deltoids).also { membrosSuperiores.addNode(it) }
        Tree(R.string.anterior_deltoids).also { deltoides.addNode(it) }
        Tree(R.string.lateral_deltoids).also { deltoides.addNode(it) }
        Tree(R.string.posterior_deltoids).also { deltoides.addNode(it) }
        val abdominais = Tree(R.string.abs).also { tronco.addNode(it) }
        Tree(R.string.rectus_abdominal).also { abdominais.addNode(it) }
        Tree(R.string.oblique_external).also { abdominais.addNode(it) }
        Tree(R.string.oblique_internal).also { abdominais.addNode(it) }
        Tree(R.string.back).also { tronco.addNode(it) }
        Tree(R.string.serratil_anterior).also { tronco.addNode(it) }
        val costas = Tree(R.string.back_).also { tronco.addNode(it) }
        Tree(R.string.transverse_abdominal).also { abdominais.addNode(it) }
        Tree(R.string.trapezium).also { costas.addNode(it) }
        Tree(R.string.rhomboids).also { costas.addNode(it) }
        Tree(R.string.spine_erectors).also { costas.addNode(it) }
        val coxas = Tree(R.string.thighs).also { membrosInferiores.addNode(it) }
        Tree(R.string.quadriceps).also { coxas.addNode(it) }
        Tree(R.string.adductors).also { coxas.addNode(it) }
        Tree(R.string.thigh_back).also { coxas.addNode(it) }
        Tree(R.string.glutes).also { membrosInferiores.addNode(it) }
        Tree(R.string.calves).also { membrosInferiores.addNode(it) }
        leafs.forEach { leaf ->
            leaf.setValueInternal(1)
        }
        musculos.sumAllNodes()
        leafs.forEach { leaf ->
            println("muscle: "+leaf.toString(context))
        }
        return leafs
    }
}
