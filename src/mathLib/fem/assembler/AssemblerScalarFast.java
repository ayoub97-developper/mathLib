package mathLib.fem.assembler;

import mathLib.fem.core.Mesh;
import mathLib.fem.core.Node;
import mathLib.fem.core.NodeRefined;
import mathLib.fem.core.NodeType;
import mathLib.fem.core.intf.AssemblerOld;
import mathLib.fem.core.intf.WeakFormOld;
import mathLib.fem.util.container.ElementList;
import mathLib.fem.util.container.NodeList;
import mathLib.func.symbolic.Variable;
import mathLib.func.symbolic.intf.MathFunc;
import mathLib.func.symbolic.intf.VecMathFunc;
import mathLib.matrix.algebra.SparseMatrixRowMajor;
import mathLib.matrix.algebra.SparseVectorHashMap;
import mathLib.matrix.algebra.intf.SparseMatrix;
import mathLib.matrix.algebra.intf.SparseVector;

public class AssemblerScalarFast implements AssemblerOld{
	protected Mesh mesh;
	protected WeakFormOld weakForm;
	protected SparseMatrix globalStiff;
	protected SparseVector globalLoad;

	public AssemblerScalarFast(Mesh mesh, WeakFormOld weakForm) {
		this.mesh = mesh;
		this.weakForm = weakForm;
		
		int dim = mesh.getNodeList().size();
		globalStiff = new SparseMatrixRowMajor(dim,dim);
		globalLoad = new SparseVectorHashMap(dim);

	}
	
	@Override
	public void assemble() {
		ElementList eList = mesh.getElementList();
		int nEle = eList.size();
		for(int i=1; i<=nEle; i++) {
			eList.at(i).adjustVerticeToCounterClockwise();
			weakForm.assembleElement(eList.at(i), 
					globalStiff, globalLoad);
			if(i%3000==0)
				System.out.println("Assemble..."+
						String.format("%.0f%%", 100.0*i/nEle));
		}
		procHangingNode(mesh);
		return;
	}
	
	@Override
	public SparseVector getLoadVector() {
		return globalLoad;
	}

	@Override
	public SparseMatrix getStiffnessMatrix() {
		return globalStiff;
	}
	
	@Override
	public void imposeDirichletCondition(MathFunc diri) {
		NodeList nList = mesh.getNodeList();
		for(int i=1;i<=nList.size();i++) {
			Node n = nList.at(i);
			if(n.getNodeType() == NodeType.Dirichlet) {
				Variable v = Variable.createFrom(diri, n, n.globalIndex);
				this.globalStiff.set(n.globalIndex, n.globalIndex, 1.0);
				double val = diri.apply(v);
				this.globalLoad.set(n.globalIndex, val);
				for(int j=1;j<=this.globalStiff.getRowDim();j++) {
					if(j!=n.globalIndex) {
						//TODO è¡Œåˆ—éƒ½éœ€è¦�ç½®é›¶
						this.globalLoad.add(j, -this.globalStiff.get(j, n.globalIndex)*val);
						this.globalStiff.set(j, n.globalIndex, 0.0);
						this.globalStiff.set(n.globalIndex, j, 0.0);
					}
				}
			}
		}
	}
	
	//äºŒç»´ï¼šåˆšåº¦çŸ©é˜µå¢žåŠ hanging nodeçº¦æ�Ÿç³»æ•°
	// nh - 0.5*n1 - 0.5*n2 = 0
	public void procHangingNode(Mesh mesh) {
		
		for(int i=1;i<=mesh.getNodeList().size();i++) {
			Node node = mesh.getNodeList().at(i);
			if(node instanceof NodeRefined) {
				NodeRefined nRefined = (NodeRefined)node;
				if(nRefined.isHangingNode()) {
					globalStiff.set(nRefined.globalIndex, nRefined.globalIndex, 1.0);
					globalStiff.set(nRefined.globalIndex,
							nRefined.constrainNodes.at(1).globalIndex,-0.5);
					globalStiff.set(nRefined.globalIndex,
							nRefined.constrainNodes.at(2).globalIndex,-0.5);
				}
			}
		}
	}
	
	@Override
	public void imposeDirichletCondition(VecMathFunc diri) {
		throw new UnsupportedOperationException();
	}	
}
