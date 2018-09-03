package mathLib.polynom.special;

import mathLib.polynom.Polynomial;
import static mathLib.polynom.Polynomial.* ;
import static mathLib.util.MathUtils.* ;

public class JacobiPoly {

	public static Polynomial jacobi (int degree, double alpha, double beta) {
		int n = degree ;
		if(n<0)
			throw new IllegalArgumentException("degree must be greater than or equal to 0") ;

		Polynomial result = ZERO ;
		for(int s=0; s<=n; s++){
			double coeff = combination(n+alpha, n-s) * combination(n+beta, s) ;
			result = result + coeff * ((X-1.0)/2.0).pow(s) * ((X+1.0)/2).pow(n-s) ;
		}
		return result ;
	}


	// for test
	public static void main(String[] args) {
		System.out.println(jacobi(5, 0, 0));
		System.out.println(LegendrePoly.legendre(5));
	}

}