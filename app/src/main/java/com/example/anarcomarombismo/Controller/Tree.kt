package com.example.anarcomarombismo.Controller

class Tree(val name: String) {
    private var value: Int
        get() = values[name] ?: 0
        set(value) {
            values[name] = value
        }
    private var superNode: Tree?
        get() = superNodes[name]
        set(value) {
            superNodes[name] = value
        }
    companion object {
        private val leafs = mutableSetOf<Tree>()
        private val subNodes = mutableMapOf<String, MutableSet<Tree>>()
        private val superNodes = mutableMapOf<String, Tree?>()
        private var values = mutableMapOf<String, Int>()
        private var isLeaf = mutableMapOf<String, Boolean>()
    }
    init {
        if (name.isNotEmpty()) {
            leafs.add(this)
            isLeaf[name] = true
        } else {
            resetTree()
        }
    }
    fun setValueInternal(value: Int) {
        values[name] = value
    }
    private fun resetTree() {
        leafs.clear()
        subNodes.clear()
        superNodes.clear()
        values.clear()
        isLeaf.clear()
    }
    fun addNode(subTree: Tree) {
        if (name.isNotEmpty()) {
            subTree.superNode = this
            if (!subNodes.containsKey(this.name)) {
                subNodes[this.name] = mutableSetOf()
            }
            subNodes[this.name]!!.add(subTree)
            isLeaf[this.name] = false
            leafs.remove(this)
        }
    }
    fun getLeafs(): Set<Tree> {
        return leafs
    }
    private fun sumChildren() {
        var sum = 0
        subNodes[name]?.forEach { child ->
            sum += child.value
        }
        values[name] = sum
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
    fun sumAllNodes() {
        sumNodes(leafs)
    }
    override fun toString(): String {
        var currentNode: Tree? = this
        var result = ""
        while (currentNode != null) {
            result = "-> ${currentNode.name}:${currentNode.value} $result"
            currentNode = currentNode.superNode
        }
        return result.trimStart('-').trim() + "\n"
    }
}