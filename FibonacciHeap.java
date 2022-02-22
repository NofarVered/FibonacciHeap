//Nofarveredn 

/*
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over integers.
 */
public class FibonacciHeap {
	private HeapNode min; // holds the current node with minimal key
	private int size; // number of nodes in the heap
	private int trees; // number of trees in the heap
	private int marked; // number of marked nodes in the heap
	private HeapNode first; // pointing to the first tree added
	private static int numOfLinks = 0; // number of links performed in this run
	private static int numOfCuts = 0; // number of cuts performed in this run

	public FibonacciHeap() {
		this.min = null; // pointer to the node with the min key in the all heap
		this.size = 0; // how many nodes there is in the heap
		this.trees = 0; // how many trees there is in the heap
		this.first = null;
		this.marked = 0; // how many nodes are marked in the all heap
	}

	/**
	 * public boolean isEmpty()
	 *
	 * precondition: none
	 * 
	 * 
	 */
	public boolean isEmpty() // O(1)
	{ // The method returns true if and only if the heap is empty.
		return (this.min == null);
	}

	/**
	 * public HeapNode insert(int key)
	 *
	 * Creates a node (of type HeapNode) which contains the given key, and inserts
	 * it into the heap.
	 * 
	 * Returns the new node created.
	 */
	public HeapNode insert(int key) // O(1)
	{ // insert a node in a lazy way in to the heap
		HeapNode node = new HeapNode(key);
		this.insertNode(node);
		this.size++;
		return node;

	}

	private void insertNode(HeapNode node) { // O(1)
		// insert a node (which can be a tree) to the heap trees
		if (this.isEmpty()) {
			this.min = node;
			this.first = node;
			node.setNext(node);
			node.setPrev(node);
		}

		else {
			HeapNode last = first.getPrev();
			node.setNext(first);
			node.setPrev(last);
			first.setPrev(node);
			last.setNext(node);
			if (node.getKey() < this.min.getKey())
				this.min = node;
			first = node;
			if (node.mark == true) {
				node.mark = false;
				this.marked--;
			}
		}
		this.trees++;

	}

	private void insertForCut(HeapNode node) { // O(1)
		// same as insertNode but with reference node to choose
		this.insertNode(node);
		if (node.mark == true) {
			node.mark = false;
			this.marked--;
		}

	}

	/**
	 * public void deleteMin()
	 *
	 * Delete the node containing the minimum key.
	 *
	 */
	public void deleteMin() // WC: O(n) Amortized: O(logn)
	{
		// seperates the min node then melds it's sons and consolidates
		HeapNode minNode = this.findMin();
		HeapNode child = minNode.child;
		HeapNode next = minNode.next;
		HeapNode prev = minNode.prev;

		if ((minNode == next) && (child == null)) { // in case there is only a single node in the heap
			this.min = null;
			this.trees = 0;
			this.first = null;
			this.marked = 0;
			return;
		}
		this.disconnectTree(minNode);
		// remove the node from the heap in O(1)
		minNode.child = null;
		if (child != null) {
			child.parent = null;
			if (next != minNode)
				this.MeldForDelMin(prev, child, next); // add the min node childs to the heap in O(1)
			else
				first = child;
		}
		this.min = null;
		this.size--;
		this.consolidate(first); // O(n) WC

	}

	private void tearTree(HeapNode tree) { // O(1)
		// tears down a tree from the heap into the bucket
		HeapNode next = tree.next;
		tree.next = null;
		tree.prev = null;
		if (next != null)
			next.prev = null;
	}

	private void consolidate(HeapNode node) { // O(n)
		/*
		 * runs over all the trees using the bucket method
		 * and merges trees of the same rank, and remerges until there are no 2 trees
		 * with the same rank
		 */
		if (node == null) {
			this.min = null;
			this.size = 0;
			this.marked = 0;
			this.first = null;
		}
		HeapNode[] buckets = new HeapNode[this.size()];
		HeapNode minCandidate = node;
		HeapNode cur = node;
		HeapNode next;
		HeapNode newTree;
		int curank;
		int maxrank = 0;
		this.trees = 0;
		cur.prev.next = null;
		while (cur != null) { // run on all trees validated by the number of trees
			cur.parent = null; // clean all trees parents - some still have the previous min node as parent
			next = cur.getNext();
			if (next == cur)
				next = null;
			curank = cur.getRank();
			this.tearTree(cur);
			if (buckets[curank] == null) { // add tree to empty bucket
				buckets[curank] = cur;
			} else {
				newTree = cur;
				while (buckets[curank] != null) { // merge until you reach an empty bucket
					if (newTree == buckets[curank])
						break;
					newTree = this.mergeTrees(buckets[curank], newTree);
					buckets[curank] = null;

					curank = newTree.getRank();
				}
				buckets[curank] = newTree;
				cur = newTree;
			}

			if (cur.getKey() < minCandidate.getKey())
				minCandidate = cur;
			if (curank > maxrank)
				maxrank = curank;
			cur = next;
		}
		for (int i = 0; i < maxrank + 1; i++) { // add all trees from the buckets back to the heap
			if (buckets[i] != null) {
				this.insertNode(buckets[i]);
			}

		}
		this.min = minCandidate;
	}

	private HeapNode mergeTrees(HeapNode tree1, HeapNode tree2) { // O(1)
		// merges 2 trees in rank n, and returns the merged tree in
		HeapNode notremoved;
		if (tree1.getKey() < tree2.getKey()) {
			notremoved = tree1;
			this.link(tree1, tree2);
		} else {
			notremoved = tree2;
			this.link(tree2, tree1);
		}
		return notremoved;
	}

	private void disconnectTree(HeapNode tree) { // O(1)
		// removes a tree from the heap
		if (tree == first) {
			first = tree.getNext();
		}
		HeapNode next = tree.next;
		HeapNode prev = tree.prev;
		next.prev = prev;
		prev.next = next;
		this.trees--;
	}

	private void link(HeapNode parent, HeapNode son) { // O(1)
		// links son as a child of parent
		// also counts the link for the overall links in the program
		HeapNode origSon = parent.getChild();
		if (origSon != null) {
			son.prev = origSon.prev;
			origSon.prev.next = son;
			origSon.prev = son;
			son.next = origSon;
		} else {
			son.prev = son;
			son.next = son;
		}
		parent.child = son;
		son.parent = parent;
		FibonacciHeap.numOfLinks++;
		parent.rank++;
	}

	private void cut(HeapNode parent, HeapNode son) { // O(1)
		// cut a node and replace parent's child
		HeapNode nextSon = son.getNext();
		if (nextSon == son) {
			nextSon = null;
		} else {
			nextSon.prev = son.prev;
			son.prev.next = nextSon;
		}
		if (parent.child == son)
			parent.child = nextSon;
		son.parent = null;
		parent.rank--;
		this.insertForCut(son);
		FibonacciHeap.numOfCuts++;

	}

	/**
	 * public HeapNode findMin()
	 *
	 *
	 */
	public HeapNode findMin() // O(1)
	{// Return the node of the heap whose key is minimal.
		return this.min;
	}

	/**
	 * public void meld (FibonacciHeap heap2)
	 *
	 * Meld the heap with heap2
	 *
	 */
	public void meld(FibonacciHeap heap2) // O(1)
	{ // we are updating this.heap
		// sum together size, marked, trees
		this.marked += heap2.get_marked();
		this.trees += heap2.get_trees();
		this.size += heap2.size();
		if (this.min.getKey() > heap2.findMin().getKey()) { // update the min pointer
			this.min = heap2.findMin();
		}
		// update 4 arcs
		this.insideMeld(this.first, heap2.first);
	}

	private void insideMeld(HeapNode node1, HeapNode node2) // O(1)
	{ // melding trees within the heap
		HeapNode last1 = node1.getPrev();
		HeapNode last2 = node2.getPrev();
		node1.setPrev(last2);
		node2.setPrev(last1);
		last2.setNext(node1);
		last1.setNext(node2);
	}

	private void MeldForDelMin(HeapNode node1, HeapNode son, HeapNode node2) // O(1)
	{ // melding the min sons right to node1 and left to node2
		// O((1)
		HeapNode lastson = son.getPrev();
		node1.setNext(son);
		son.setPrev(node1);
		node2.setPrev(lastson);
		lastson.setNext(node2);
	}

	public int get_marked() { // O(1)
		// return how many marked nodes there are in the all heap
		return this.marked;
	}

	public int get_trees() { // O(1)
		// return how many trees there are in the all heap
		return this.trees;
	}

	/**
	 * public int size()
	 *
	 * Return the number of elements in the heap
	 * 
	 */
	public int size() // O(1)
	{
		return this.size;
	}

	/**
	 * public int[] countersRep()
	 *
	 * Return a counters array, where the value of the i-th entry is the number of
	 * trees of order i in the heap.
	 * 
	 */
	public int[] countersRep() // O(n)
	{ // Return a counters array, where the value of the i-th entry is the number of
		// trees of order i in the heap.
		int[] arr = new int[0]; // an empty heap case
		if (this.isEmpty()) {
			return arr;
		}
		arr = new int[this.searcMaxRank() + 1]; // Backup: arr = new int[this.maxRank];
		for (int i = 0; i < arr.length; i++) { // zeros in all arr //O(len(arr))
			arr[i] = 0;
		}
		int minrank = this.findMin().getRank();
		arr[minrank] = 1; // dealing the min node
		HeapNode p = null; // pointer for roots
		p = this.min.getNext();
		while (p != this.min) { // O(trees)
			arr[p.getRank()] += 1;
			p = p.getNext();
		}
		return arr;
	}

	private int searcMaxRank() { // O(n)
		// search for the tree with the maximal rank
		if (this.isEmpty()) {
			return 0;
		}
		int maxrank = this.min.getRank();
		HeapNode cur = this.min.getNext();
		while (cur != this.min) {
			if (cur.getRank() > maxrank) {
				maxrank = cur.getRank();
			}
			cur = cur.getNext();
		}
		return maxrank;
	}

	/**
	 * public void delete(HeapNode x)
	 *
	 * Deletes the node x from the heap.
	 *
	 */
	public void delete(HeapNode x) // O(n) in WC and O(log n) amortized
	// Deletes the node x from the heap.
	{
		this.decreaseKey(x, x.getKey() - this.findMin().getKey() + 10000);
		this.deleteMin();
	}

	/**
	 * public void decreaseKey(HeapNode x, int delta)
	 *
	 * The function decreases the key of the node x by delta. The structure of the
	 * heap should be updated
	 * to reflect this chage (for example, the cascading cuts procedure should be
	 * applied if needed).
	 */
	public void decreaseKey(HeapNode x, int delta) // WF: O(numOfCuts) amortized: O(1)
	{
		int newkey = x.getKey() - delta;
		x.key = newkey;
		if (x.parent == null) { // if the node is already a root, no cut needed
			if (x.key < this.findMin().getKey())
				this.min = x;
		} else {
			if (x.key < x.parent.getKey())
				this.cascade(x);
		}
	}

	private void cascade(HeapNode node) { // WC: O(numOfCuts) amortized: O(1)
		// cut the node from it's tree and mark the parent, if its parent is marked -
		// cut it too
		HeapNode parent = node.getParent();
		this.cut(parent, node);

		if (parent.mark) {
			this.cascade(parent); // cascade up to marked parent
		} else {
			if (parent.getParent() != null) { // update mark for non root parent
				parent.mark = true;
				this.marked++;
			}
		}

	}

	/**
	 * public int potential()
	 *
	 * This function returns the current potential of the heap, which is:
	 * Potential = #trees + 2*#marked
	 * The potential equals to the number of trees in the heap plus twice the number
	 * of marked nodes in the heap.
	 */
	public int potential() // O(1)
	{
		return this.trees + 2 * this.marked;
	}

	/**
	 * public static int totalLinks()
	 *
	 * This static function returns the total number of link operations made during
	 * the run-time of the program.
	 * A link operation is the operation which gets as input two trees of the same
	 * rank, and generates a tree of
	 * rank bigger by one, by hanging the tree which has larger value in its root on
	 * the tree which has smaller value
	 * in its root.
	 */
	public static int totalLinks() // O(1)
	{
		// get the link count
		return FibonacciHeap.numOfLinks;
	}

	public static void zeroLinks()// O(1)
	{
		// zero the number of links to recount within the same code
		FibonacciHeap.numOfLinks = 0;
	}

	/**
	 * public static int totalCuts()
	 *
	 * This static function returns the total number of cut operations made during
	 * the run-time of the program.
	 * A cut operation is the operation which diconnects a subtree from its parent
	 * (during decreaseKey/delete methods).
	 */
	public static int totalCuts() {
		// get the cut count
		return FibonacciHeap.numOfCuts;
	}

	public static void zeroCuts()// O(1)
	{
		// zero the number of cuts to recount within the same code
		FibonacciHeap.numOfCuts = 0;
	}

	/**
	 * public static int[] kMin(FibonacciHeap H, int k)
	 *
	 * This static function returns the k minimal elements in a binomial tree H.
	 * The function should run in O(k*deg(H)).
	 * You are not allowed to change H.
	 */
	public static FibonacciHeap Heap_min = new FibonacciHeap(); // heap candidates for a min key

	public static int[] kMin(FibonacciHeap H, int k) {
		// O(k*log(deg H))
		int[] arr = new int[k]; // zero is the defult value
		if (arr.length == 0) { // k is zero
			return arr;
		}
		arr[0] = H.findMin().getKey(); // the root
		H.findMin().setinOut();
		HeapNode T = H.findMin(); // pointer for searching
		for (int i = 1; i < k; i++) { // (k-1) iteration
			child_to_heap(T);
			bro_to_heap(T);
			arr[i] = Heap_min.findMin().getKey(); // O(1)
			T = Heap_min.findMin().getCopy(); // O(1)
			Heap_min.deleteMin(); // O(log(number of nodes in Heap_min<=k)) = O(log k)
		}
		return arr;
	}

	public static void child_to_heap(HeapNode P) {
		// update Heap_min with the minimal P's child
		// O(P.rank) <= O(deg H)
		if (P.getChild() == null) { // no child to the node
			return;
		}
		HeapNode P1 = P.getChild();// pointer for searching the min-node
		HeapNode minNode = P1; // Temporary min node
		P1 = P1.getNext();
		while (P1 != P.getChild()) {
			if (P1.getKey() < minNode.getKey() && P1.getinOut() == 0) {
				minNode = P1;
			}
			P1 = P1.getNext();
		}
		HeapNode HM = Heap_min.insert(minNode.getKey()); // O(1) //insert a copy of node-child to Heap_min
		HM.setCopy(minNode); // pointer to the orighinal node in H
		minNode.setinOut(); // for knowing this node is alredy IN the Heap_min
	}

	public static void bro_to_heap(HeapNode P) {
		// update Heap_min with the ninimal P's brother
		// O(deg H)
		if (P.getNext() == null) { // no brothers to the node
			return;
		}
		HeapNode P1 = P.getNext();// we know P has a brother
		while (P1 != P && P1.getinOut() == 1) {
			P1 = P1.getNext();
		}
		if (P1 == P) {
			return;
		}
		HeapNode minNode = P1; // Temporary min node
		while (P1 != P) {
			if (P1.getKey() < minNode.getKey() && P1.getinOut() == 0) {
				minNode = P1;
			}
			P1 = P1.getNext();
		}
		HeapNode HM = Heap_min.insert(minNode.getKey()); // O(1) //insert a copy of node-child to Heap_min
		HM.setCopy(minNode); // pointer to the orighinal node in H
		minNode.setinOut(); // for knowing this node is alredy IN the Heap_min
	}

	/**
	 * public class HeapNode
	 * 
	 * If you wish to implement classes other than FibonacciHeap
	 * (for example HeapNode), do it in this file, not in
	 * another file
	 * 
	 */

	public class HeapNode {

		private int key; // node's key
		private HeapNode parent; // the parent of the node
		private HeapNode prev; // the previous node
		private HeapNode next; // the next node
		private HeapNode child; // the leftest child
		private boolean mark; // mark for Cascading cuts
		private int rank; // how many children there is
		private HeapNode copy; // pointer to the copy-node in the original heap - for Kmin function !!!
		private int inOut; // 0 for not in tha Heap_min, 1 otherwise - for Kmin function !!!

		public HeapNode(int key) { // constructor O(1)
			this.key = key;
			this.parent = null;
			this.child = null;
			this.next = null;
			this.prev = null;
			this.mark = false;
			this.rank = 0;
			this.copy = null;
			this.inOut = 0;
		}

		public int getKey() { // O(1)
			// return node's key
			return this.key;
		}

		public int getinOut() { // O(1)
			// return 0 if the node is not in heap_min
			return this.inOut;
		}

		public void setinOut() {
			// set to inOut to 1 if there is a copy in heap_node
			this.inOut = 1;
		}

		public HeapNode getCopy() { // O(1)
			// return a pointer to the copy in the original heap
			return this.copy;
		}

		public void setCopy(HeapNode node) { // O(1)
			// update the copy to the original heap
			this.copy = node;
		}

		public int getRank() { // O(1)
			// return root node's rank
			return this.rank;
		}

		public HeapNode getNext() {// O(1)
			// return a pointer to the next node
			return this.next;
		}

		public HeapNode getPrev() {// O(1)
			// return a pointer to the previous node
			return this.prev;
		}

		public HeapNode getChild() {// O(1)
			// return a pointer to the leftest child node
			return this.child;
		}

		public HeapNode getParent() {// O(1)
			// return a pointer to the parent node
			return this.parent;
		}

		public void setPrev(HeapNode node) {// O(1)
			// update the pointer to the previous node
			this.prev = node;
		}

		public void setNext(HeapNode node) {// O(1)
			// update the pointer to the next node
			this.next = node;
		}

	}

}