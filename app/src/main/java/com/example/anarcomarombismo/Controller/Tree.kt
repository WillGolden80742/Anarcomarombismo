package com.example.anarcomarombismo.Controller

class Tree(val name: String) {
    var superNode: Tree? = null
    var leaf: Boolean = true
    var value: Int = 0
    companion object {
        val Leafs = mutableSetOf<Tree>()
        val subNode = mutableMapOf<String, MutableSet<Tree>>()
    }

    init {
        if (!name.isEmpty() && leaf) {
            Leafs.add(this)
        }
    }
    fun addNode(subTree: Tree) {
        if (!name.isEmpty()) {
            subTree.superNode = this
            if (!subNode.containsKey(this.name)) {
                subNode[this.name] = mutableSetOf()
            }
            subNode[this.name]!!.add(subTree)
            this.leaf = false
            Leafs.remove(this)
        }
    }
    fun getLeafs(): Set<Tree> {
        return Leafs
    }
    override fun toString(): String {
        var currentNode: Tree? = this
        var result = ""
        while (currentNode != null) {
            result = "-> ${currentNode.name} $result"
            currentNode = currentNode.superNode
        }
        return result.trimStart('-').trim() + "\n"
    }

}