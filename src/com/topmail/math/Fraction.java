package com.topmail.math;

import java.math.BigDecimal;


/**
 * Classe permettant d'effectuer des calculs rationnels en valeur exacte.<br>
 * Ex : (1/4) % (1/10) = (5/100)<br>
 * Ce qui n'est pas possible en double ni en BigDecimal : 0.25 % 0.1 = 0.04999999999999999<br>
 *
 * <ul>
 * <li>Valeurs stockées en long</li>
 * <li>+INF, -INF, NaN sont gérés</li>
 * </ul>
 *
 * @author Florent FRADET
 * @since 25/03/2012
 */
public class Fraction {
	public static final long		NAN			= Long.MIN_VALUE + 1;

	public static final Fraction	ZERO		= new Fraction();		// 0
	public static final Fraction	ONE_HALF	= new Fraction(1, 2);	// 1/2
	public static final Fraction	ONE			= new Fraction(1, 1);	// 1

	private static boolean			ms_bVerbose	= false;

	private long					m_iNumerator;						// Numerator
	private long					m_iDenominator;					// Denominator


	public static void setVerbose(boolean b) {
		ms_bVerbose = b;
	}


	public Fraction() // Equals Zero
	{
		m_iNumerator = 0;
		m_iDenominator = 1;
	}


	public Fraction(long n, long d) {
		if (d < 0) {
			d = -d;
			n = -n;
		}

		m_iNumerator = n;
		m_iDenominator = d;

		reduceThis();
	}


	public Fraction(long n) {
		m_iNumerator = n;
		m_iDenominator = 1;
	}


	public Fraction(double value) {
		fromDouble(value);
	}


	public Fraction(BigDecimal n) {
		fromDouble(n.doubleValue());
	}


	public Fraction(Fraction f) // constructor from another Fraction object
	{
		m_iNumerator = f.m_iNumerator;
		m_iDenominator = f.m_iDenominator;
	}


	public Fraction(String sNum) // Formats : "-x.x", "x.x", "-x,x", "x,x", ".x", ",x", "-.x", "-,x"
	{
		fromString(sNum);
	}


	public static Fraction of(double d) {
		return new Fraction(d);
	}


	public static Fraction of(long d) {
		return new Fraction(d);
	}


	public static Fraction of(BigDecimal d) {
		return new Fraction(d);
	}


	private void fromDouble(double value) {
		int sign = (value >= 0.0) ? 1 : -1;
		value = Math.abs(value);

		if ((value > Integer.MAX_VALUE) || (Double.isNaN(value))) {
			throw new ArithmeticException("The value must not be greater than Integer.MAX_VALUE nor NaN");
		}

		int wholeNumber = (int) value;
		value -= wholeNumber;
		int numer0 = 0;
		int denom0 = 1;
		int numer1 = 1;
		int denom1 = 0;
		int numer2 = 0;
		int denom2 = 0;
		int a1 = (int) value;
		int a2 = 0;
		double x1 = 1.0;
		double x2 = 0.0;
		double y1 = value - a1;
		double y2 = 0.0;
		double delta2 = Double.MAX_VALUE;
		int i = 1;
		double delta1;

		do {
			delta1 = delta2;
			a2 = (int) (x1 / y1);
			x2 = y1;
			y2 = x1 - (a2 * y1);
			numer2 = (a1 * numer1) + numer0;
			denom2 = (a1 * denom1) + denom0;
			double fraction = (double) numer2 / (double) denom2;
			delta2 = Math.abs(value - fraction);
			a1 = a2;
			x1 = x2;
			y1 = y2;
			numer0 = numer1;
			denom0 = denom1;
			numer1 = numer2;
			denom1 = denom2;
			i++;
		} while ((delta1 > delta2) && (denom2 <= 10000) && (denom2 > 0) && (i < 25));

		if (i == 25) {
			throw new ArithmeticException("Unable to convert double to fraction");
		}

		m_iNumerator = (numer0 + ((long) wholeNumber * denom0)) * (long) sign;
		m_iDenominator = denom0;

		reduceThis();
	}


	// Convertit un nombre décimal
	// Détecte les nombres à partie décimale cyclique (ex : 1.3333333 = 4/3 ; 3.2828283 = 325/99).
	// Détecte les nombres à partie décimale cyclique et acyclique (ex : 90.136363636 = 1983/22)
	private void fromStringFloat(String sNum) {
		double d = Double.parseDouble(sNum);

		// Méthode mathématique itérative optimisée
		fromDouble(d);

		/*
		 * // Partie opérant par identification de redondances cycliques (méthode manuelle non itérative) // Nécessite un paramètre "radix" (base n) et "prec" (précision, nombre de
		 * décimales maximum acceptable)
		 *
		 * String sNumInt = sNum.replaceAll("\\.", "");
		 *
		 * int iPoint = sNum.indexOf('.'); int iNbDec = sNum.length() - (iPoint + 1);
		 *
		 * boolean bCyclicDecimal = false; int iTokenLen = 0; String sToken = null; int iStart = 0;
		 *
		 * if (iNbDec > prec) { // Plus de 'prec' décimales -> on considère que le nombre de décimales est infini. // On retire la dernière décimale car c'est un arrondi sNum =
		 * sNum.substring(0, sNum.length()-1); String sDecimales = sNum.substring(iPoint + 1);
		 *
		 * if (ms_bVerbose) System.out.println("sDecimales = " + sDecimales + " len = " + sDecimales.length());
		 *
		 * // Rechercher la chaine périodique
		 *
		 * boolean bFound = false; for ( ; (!bFound) && (iStart < sDecimales.length()-1) ; iStart++) { if (ms_bVerbose) System.out.println("iStart = " + iStart);
		 *
		 * iTokenLen = 0; while (iTokenLen < sDecimales.length() - 1 - iStart) { iTokenLen++; sToken = sDecimales.substring(iStart, iStart + iTokenLen);
		 *
		 * if (ms_bVerbose) System.out.println("Token = " + sToken + " ; len = " + iTokenLen);
		 *
		 * bFound = true; int i; for (i = iStart; (i <= sDecimales.length() - iTokenLen) && (bFound); i += iTokenLen) { bFound = (sDecimales.indexOf(sToken, i) == i); }
		 *
		 * if (bFound) { // Tester éventuellement la dernière position avec le token partiel if ((i > sDecimales.length() - iTokenLen) && (i < sDecimales.length())) { int iLen =
		 * sDecimales.length() - i; bFound = (sDecimales.indexOf(sToken.substring(0, iLen), i) == i);
		 *
		 * if (ms_bVerbose) { System.out.println("Partial token : iLen = " + iLen + " ; i = " + i); System.out.println("found ? " + bFound); } }
		 *
		 * if (bFound) break; } } }
		 *
		 * bCyclicDecimal = (iTokenLen < sDecimales.length()); if (ms_bVerbose) { System.out.println("Found token len = " + iTokenLen + " ; token = " + sToken);
		 * System.out.println("bCyclicDecimal = " + bCyclicDecimal); } }
		 *
		 * if (!bCyclicDecimal) { // Nombre irrationnel ; stockage de la valeur rationnelle (tronquée) m_iNumerator = Long.parseLong(sNumInt, radix); m_iDenominator =
		 * simplePower(radix, iNbDec); } else { // Nombre rationnel String sIntPart = sNum.substring(0, iPoint);
		 *
		 * // 0.999999999999...(infini) == 1 == 10(0)...(infini) / 9(9)...(infini) m_iDenominator = simplePower(radix, iTokenLen)-1;
		 *
		 * if (iStart > 0) { // Gestion des décimales non périodiques (ex : Le "7" de "0.718281828") String sDecAcyclic = sNum.substring(iPoint+1, iPoint + iStart);
		 *
		 * if (ms_bVerbose) System.out.println("Acyclic decimals : '" + sDecAcyclic + "'");
		 *
		 * // Ajoute les décimales acycliques à la partie entière (chaine de caractère) sIntPart += sDecAcyclic;
		 *
		 * m_iNumerator = Long.parseLong(sToken, radix) + Long.parseLong(sIntPart, radix) * m_iDenominator;
		 *
		 * // Ajoute un facteur "radix" au dénomitateur pour recaler la virgule après injection des décimales acycliques dans la partie entière m_iDenominator *= simplePower(radix,
		 * sDecAcyclic.length()); } else { // Le nombre n'a pas de décimales non périodiques m_iNumerator = Long.parseLong(sToken, radix) + Long.parseLong(sIntPart, radix) *
		 * m_iDenominator; } }
		 *
		 * reduceThis();
		 */
	}


	/*
	 * Fonction utilisée par la méthode de parsing manuelle non itérative private static long simplePower(long a, int b) { long res = 1; for (int i = 0; i < b; i++) { res *= a; }
	 *
	 * return res; }
	 */

	public void fromStringInteger(String sNum, int radix) {
		m_iNumerator = Long.parseLong(sNum, radix);
		m_iDenominator = 1;
	}


	public void fromStringFrac(String sNum, int radix) {
		int iDiv = sNum.indexOf('/');

		if (ms_bVerbose) {
			System.out.println("iDiv = " + iDiv);
		}

		String sNumerator = sNum.substring(0, iDiv);

		if (ms_bVerbose) {
			System.out.println("sNumerator = " + sNumerator);
		}

		String sDenominator = sNum.substring(iDiv + 1);

		if (ms_bVerbose) {
			System.out.println("sDenominator = " + sDenominator);
		}

		m_iNumerator = Long.parseLong(sNumerator, radix);
		m_iDenominator = Long.parseLong(sDenominator, radix);

		reduceThis();
	}


	// Base 10
	public void fromString(String sNum) {
		sNum = sNum.replace(',', '.');
		String sNumClean = cleanString(sNum);

		if (sNumClean.indexOf('.') >= 0) {
			fromStringFloat(sNumClean);
		} else if (sNumClean.indexOf('/') >= 0) {
			fromStringFrac(sNumClean, 10);
		} else {
			fromStringInteger(sNumClean, 10);
		}
	}


	private static String cleanString(String s) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if ((c == '.') || (c == '-') || (c == '/') || ((c >= '0') && (c <= '9')) || ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))) {
				buf.append(c);
			}
		}

		return buf.toString();
	}


	// Plus Grand Commun Diviseur
	// Sert à simplifier une fraction
	// Ex : 228/8 = (228/4) / (8/4) = 57/2 car PGCD(228, 8) = 4
	private static long pgcd(long x, long y) {
		if (x == 0) {
			return y;
		}

		if (x < y) {
			long iTmp = x;
			x = y;
			y = iTmp;
		}

		long r = x % y;

		while (r != 0) {
			x = y;
			y = r;
			r = x % y;
		}

		return y;
	}


	private void reduceThis() {
		if (m_iDenominator < 0) {
			m_iNumerator = -m_iNumerator;
			m_iDenominator = -m_iDenominator;
		}

		long iPgcd = pgcd(m_iNumerator, m_iDenominator);
		if (iPgcd < 0) {
			iPgcd = -iPgcd;
		}

		m_iNumerator /= iPgcd;
		m_iDenominator /= iPgcd;
	}


	private static Fraction reduce(long n, long d) {
		if (d < 0) {
			d = -d;
			n = -n;
		}

		long iPgcd = pgcd(n, d);

		if (iPgcd < 0) {
			iPgcd = -iPgcd;
		}

		d = d / iPgcd;
		n = n / iPgcd;

		return new Fraction(n, d);
	}


	/**
	 * Truncates a double and returns a long value<br>
	 *
	 * @return a truncated value
	 */
	public static long truncate(double value) {
		Fraction f = new Fraction(value);
		return f.truncateToLong();
	}


	/**
	 * Truncates a double and returns a long value<br>
	 *
	 * @param value
	 *            the value tu truncate
	 * @param nbDec
	 *            the number of decimals
	 * @return a truncated value
	 */
	public static double truncateDec(double value, int nbDec) {
		Fraction fExp = new Fraction(10);
		fExp = fExp.pow(nbDec);

		Fraction fFixed = new Fraction(value).mul(fExp);
		fFixed = fFixed.truncate().div(fExp);
		return fFixed.toDouble();
	}


	/**
	 * Truncates this and returns a new Fraction instance<br>
	 * "this" is not modified by the operation
	 *
	 * @return a truncated value
	 */
	public Fraction truncate() {
		return new Fraction(truncateToLong());
	}


	/**
	 * Truncates this and returns a long value<br>
	 * "this" is not modified by the operation
	 *
	 * @return a truncated value
	 */
	public long truncateToLong() {
		// Infinity
		if (m_iDenominator == 0) {
			return (m_iNumerator > 0) ? Long.MAX_VALUE : Long.MIN_VALUE;
		}

		long i = m_iNumerator / m_iDenominator;
		return i;
	}


	/**
	 * Rounds the fraction at the nbDec'th decimal
	 *
	 * @param nbDec
	 * @return
	 */
	public Fraction round(int nbDec) {
		long lPow = 1;
		for (int i = nbDec; i > 0; i--) {
			lPow *= 10;
		}
		Fraction fFixed = mul(lPow);
		if (ms_bVerbose) {
			System.out.println("fFixed = " + fFixed.toString());
		}
		Fraction fRem = fFixed.mod(Fraction.ONE);
		if (ms_bVerbose) {
			System.out.println("fRem = " + fRem.toString());
		}
		Long lFixed = fFixed.truncateToLong();

		if (fRem.isGTE(Fraction.ONE_HALF)) {
			lFixed++;
		}

		if (fRem.isLT(Fraction.ONE_HALF.neg())) {
			lFixed--;
		}

		Fraction f = new Fraction(lFixed, lPow);

		return f;
	}


	/**
	 * Floors a double and returns a long value<br>
	 *
	 * @return the closest to negative infinity value
	 */
	public static long floor(double value) {
		Fraction f = new Fraction(value);
		return f.floorToLong();
	}


	/**
	 * Floors this and returns a new Fraction instance<br>
	 * "this" is not modified by the operation
	 *
	 * @return the closest to negative infinity value
	 */
	public Fraction floor() {
		return new Fraction(floorToLong());
	}


	/**
	 * Floors this and returns a long value<br>
	 * "this" is not modified by the operation
	 *
	 * @return the closest to negative infinity value
	 */
	public long floorToLong() {
		// Infinity
		if (m_iDenominator == 0) {
			return (m_iNumerator > 0) ? Long.MAX_VALUE : Long.MIN_VALUE;
		}

		long i = m_iNumerator / m_iDenominator;
		//
		if ((m_iNumerator < 0) && (m_iDenominator > 1)) {
			i--;
		}

		return i;
	}


	/**
	 * Ceils a double and returns a long value<br>
	 *
	 * @return the closest to positive infinity value
	 */
	public static long ceil(double value) {
		Fraction f = new Fraction(value);
		return f.ceilToLong();
	}


	/**
	 * Ceils this and returns a new Fraction instance<br>
	 * "this" is not modified by the operation
	 *
	 * @return the closest to positive infinity value
	 */
	public Fraction ceil() {
		return new Fraction(ceilToLong());
	}


	/**
	 * Ceils this and returns a long value<br>
	 * "this" is not modified by the operation
	 *
	 * @return the closest to positive infinity value
	 */
	public long ceilToLong() {
		// Infinity
		if (m_iDenominator == 0) {
			return (m_iNumerator > 0) ? Long.MAX_VALUE : Long.MIN_VALUE;
		}

		long i = m_iNumerator / m_iDenominator;
		//
		if ((m_iNumerator > 0) && (m_iDenominator > 1)) {
			i++;
		}

		return i;
	}


	/**
	 *
	 * @param pow
	 * @return
	 */
	public Fraction pow(int pow) {
		//
		if (pow < 0) {
			return new Fraction(0, 0); // NaN
		}

		long iNum = 1;
		long iDen = 1;
		//
		for (int i = pow; i > 0; i--) {
			iNum *= m_iNumerator;
			iDen *= m_iDenominator;
		}

		return new Fraction(iNum, iDen);
	}


	/**
	 *
	 * @return
	 */
	public Fraction abs() {
		Fraction result = new Fraction(this);

		if (m_iNumerator < 0) {
			result.m_iNumerator = -result.m_iNumerator;
		}

		if (m_iDenominator < 0) {
			result.m_iDenominator = -result.m_iDenominator;
		}

		return result;
	}


	/**
	 *
	 * @return
	 */
	public Fraction neg() {
		Fraction result = new Fraction(this);
		result.m_iNumerator = -result.m_iNumerator;

		return result;
	}


	/**
	 *
	 * @param b
	 * @return
	 */
	public Fraction add(Fraction b) {
		long i1 = (this.m_iNumerator * b.m_iDenominator) + (b.m_iNumerator * this.m_iDenominator);
		long i2 = this.m_iDenominator * b.m_iDenominator;

		return reduce(i1, i2);
	}


	/**
	 *
	 * @param b
	 * @return
	 */
	public Fraction add(long b) {
		long i1 = m_iNumerator + (b * m_iDenominator);

		return reduce(i1, m_iDenominator);
	}


	/**
	 *
	 * @param b
	 * @return
	 */
	public Fraction sub(Fraction b) {
		long i1 = (this.m_iNumerator * b.m_iDenominator) - (b.m_iNumerator * this.m_iDenominator);
		long i2 = this.m_iDenominator * b.m_iDenominator;

		return reduce(i1, i2);
	}


	/**
	 *
	 * @param b
	 * @return
	 */
	public Fraction sub(long b) {
		long i1 = m_iNumerator - (b * m_iDenominator);

		return reduce(i1, m_iDenominator);
	}


	/**
	 *
	 * @param b
	 * @return
	 */
	public Fraction mul(Fraction b) {
		long i1 = this.m_iNumerator * b.m_iNumerator;
		long i2 = this.m_iDenominator * b.m_iDenominator;

		return reduce(i1, i2);
	}


	/**
	 *
	 * @param b
	 * @return
	 */
	public Fraction mul(long b) {
		long i1 = m_iNumerator * b;

		return reduce(i1, m_iDenominator);
	}


	/**
	 *
	 * @param b
	 * @return
	 */
	public Fraction div(Fraction b) {
		long i1 = this.m_iNumerator * b.m_iDenominator;
		long i2 = this.m_iDenominator * b.m_iNumerator;

		return reduce(i1, i2);
	}


	/**
	 *
	 * @param b
	 * @return
	 */
	public Fraction div(long b) {
		long i = m_iDenominator * b;

		return reduce(m_iNumerator, i);
	}


	/**
	 *
	 * @param b
	 * @return
	 */
	public Fraction mod(Fraction b) {
		Fraction f = div(b);
		long iIntegerPart = f.toLong();

		f = f.sub(iIntegerPart);

		return f.mul(b);
	}


	/**
	 *
	 * @param b
	 * @return
	 */
	public Fraction mod(long b) {
		Fraction f = div(b);
		long iIntegerPart = f.toLong();

		f = f.sub(iIntegerPart);

		return f.mul(b);
	}


	/**
	 *
	 * @return
	 */
	public String toStringDump() {
		if ((m_iNumerator > m_iDenominator) && (m_iDenominator > 1)) {
			return (m_iNumerator + "/" + m_iDenominator + " = " + (m_iNumerator / m_iDenominator) + " + " + (m_iNumerator % m_iDenominator) + "/" + m_iDenominator);
		}

		return (m_iNumerator + "/" + m_iDenominator);
	}


	/**
	 *
	 * @return
	 */
	public String toStringFrac() {
		return (m_iNumerator + "/" + m_iDenominator);
	}


	@Override
	public String toString() {
		return String.valueOf(toDouble());
	}


	/**
	 *
	 * @return
	 */
	public Long toLong() {
		if (m_iDenominator != 0) {
			return Long.valueOf(m_iNumerator / m_iDenominator);
		}

		if (m_iNumerator > 0) {
			return Long.valueOf(Long.MAX_VALUE);
		}

		if (m_iNumerator < 0) {
			return Long.valueOf(Long.MIN_VALUE);
		}

		return Long.valueOf(NAN);
	}


	/**
	 *
	 * @return
	 */
	public long longValue() {
		if (m_iDenominator != 0) {
			return m_iNumerator / m_iDenominator;
		}

		if (m_iNumerator > 0) {
			return Long.MAX_VALUE;
		}

		if (m_iNumerator < 0) {
			return Long.MIN_VALUE;
		}

		return NAN;
	}


	/**
	 *
	 * @return
	 */
	public Double toDouble() {
		if (m_iDenominator != 0) {
			return new Double((double) m_iNumerator / (double) m_iDenominator);
		}

		if (m_iNumerator > 0) {
			return new Double(Double.POSITIVE_INFINITY);
		}

		if (m_iNumerator < 0) {
			return new Double(Double.NEGATIVE_INFINITY);
		}

		return new Double(Double.NaN);
	}


	/**
	 *
	 * @return
	 */
	public double doubleValue() {
		if (m_iDenominator != 0) {
			return (double) m_iNumerator / (double) m_iDenominator;
		}

		if (m_iNumerator > 0) {
			return Double.POSITIVE_INFINITY;
		}

		if (m_iNumerator < 0) {
			return Double.NEGATIVE_INFINITY;
		}

		return Double.NaN;
	}


	/**
	 *
	 * @return
	 */
	public boolean isZero() {
		return (m_iNumerator == 0) && (m_iDenominator != 0);
	}


	/**
	 *
	 * @return
	 */
	public boolean isNaN() {
		return (m_iNumerator == 0) && (m_iDenominator == 0);
	}


	/**
	 *
	 * @return
	 */
	public boolean isPositiveInfinite() {
		return (m_iNumerator > 0) && (m_iDenominator == 0);
	}


	/**
	 *
	 * @return
	 */
	public boolean isNegativeInfinite() {
		return (m_iNumerator < 0) && (m_iDenominator == 0);
	}


	/**
	 *
	 * @return
	 */
	public boolean isInfinite() {
		return (m_iNumerator != 0) && (m_iDenominator == 0);
	}


	/**
	 *
	 * @param b
	 * @return this - b (numerator)
	 */
	public long compareTo(long b) {
		long iDen = b * m_iDenominator;

		return (m_iNumerator - iDen);
	}


	/**
	 *
	 * @param b
	 * @return this - b (numerator)
	 */
	public long compareTo(Fraction b) {
		long i1 = m_iNumerator;
		long i2 = b.m_iNumerator;

		if (b.m_iDenominator != m_iDenominator) {
			i1 *= b.m_iDenominator;
			i2 *= m_iDenominator;
		}

		return (i1 - i2);
	}


	/**
	 *
	 * @param b
	 * @return true if this == b
	 */
	public boolean equals(Fraction b) {
		return compareTo(b) == 0;
	}


	/**
	 * Greater Than
	 *
	 * @param b
	 * @return true if this > b
	 */
	public boolean isGT(Fraction b) {
		return compareTo(b) > 0;
	}


	/**
	 * Greater Than or Equals
	 *
	 * @param b
	 * @return true if this > b
	 */
	public boolean isGTE(Fraction b) {
		return compareTo(b) >= 0;
	}


	/**
	 * Lower Than
	 *
	 * @param b
	 * @return true if this > b
	 */
	public boolean isLT(Fraction b) {
		return compareTo(b) < 0;
	}


	/**
	 * Lower Than or Equals
	 *
	 * @param b
	 * @return true if this > b
	 */
	public boolean isLTE(Fraction b) {
		return compareTo(b) <= 0;
	}

}
