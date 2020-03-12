// BV Ue2 WS2019/20 Vorgabe
//
// Copyright (C) 2017 by Klaus Jung
// All rights reserved.
// Date: 2017-07-15

package bv_ws1920;


public class GeometricTransform {

	public enum InterpolationType { 
		NEAREST("Nearest Neighbour"), 
		BILINEAR("Bilinear");
		
		private final String name;       
	    private InterpolationType(String s) { name = s; }
	    public String toString() { return this.name; }
	};
	
	public void perspective(RasterImage src, RasterImage dst, double angle, double perspectiveDistortion, InterpolationType interpolation) {
		switch(interpolation) {
		case NEAREST:
			perspectiveNearestNeighbour(src, dst, angle, perspectiveDistortion);
			break;
		case BILINEAR:
			perspectiveBilinear(src, dst, angle, perspectiveDistortion);
			break;
		default:
			break;	
		}
		
	}

	/**
	 * @param src source image
	 * @param dst destination Image
	 * @param angle rotation angle in degrees
	 * @param perspectiveDistortion amount of the perspective distortion 
	 */
	public void perspectiveNearestNeighbour(RasterImage src, RasterImage dst, double angle, double perspectiveDistortion) {
		// TODO: implement the geometric transformation using nearest neighbour image rendering
		double s = perspectiveDistortion;
		double rad = Math.toRadians(angle);
	
		//Verlauf awal
		for(int y=0; y<dst.height;y++){
			for(int x=0; x<dst.width;x++){
				int pos = y * dst.width + x;
				
				//Translation -> Pixel in src (Ursprung an der Ecke) und dst (Ursprung in der Mitte) zu vergleichen
				int Xd = x-(dst.width/2); //x dikurang setengah width supaya Ursprung jadi di Mitte
				int Yd = y-(dst.height/2); //zB: src (0,0) = dst (-5,-5)
						
				//Transformation (Verzerrung von Xd & Yd)
				//Formell: Xd*(s*sin()*Xs+1) = cos()*Xs
				//		  (Xd*s*sin()*Xs)+Xd = cos()*Xs
				//        (Xd*s*sin()-cos())*Xs = -Xd
				//		  Xs = -Xd / (Xd*s*sin()-cos())
				//		  Xs = Xd / (-Xd*s*sin()+cos())
				double Xs = Xd/(Math.cos(rad)-Xd*s*Math.sin(rad)); //nilai Xd di 'rusak'
				double Ys = Yd*(s*Math.sin(rad)*Xs+1);
				
				//Translation zuruecksetzen
				double x_n = Math.round(Xs+(src.width/2)); //posisi dibalikin lagi ke x
				double y_n = Math.round(Ys+(src.height/2));
			
				int pos_n = (int)y_n * src.width + (int)x_n;
				
				if(x_n>=0 && x_n<src.width && y_n>=0 && y_n<src.height){
					dst.argb[pos] = src.argb[pos_n]; //Zielkoordinat liegt innerhalb der Randbehandlung
				}
				else
					dst.argb[pos] = 0xFFFFFFFF; //Zielkoordinate ausser Randbehandlung werden als weiss dargestellt
			}
		}
	}
	
	/**
	 * @param src source image
	 * @param dst destination Image
	 * @param angle rotation angle in degrees
	 * @param perspectiveDistortion amount of the perspective distortion 
	 */
	public void perspectiveBilinear(RasterImage src, RasterImage dst, double angle, double perspectiveDistortion) {
		// TODO: implement the geometric transformation using bilinear interpolation
		double s = perspectiveDistortion;
		double rad = Math.toRadians(angle);
	
		for(int y=0; y<dst.height;y++){
			for(int x=0; x<dst.width;x++){
				int pos = y * dst.width + x;
				
				//Translation
				int Xd = x-(dst.width/2);
				int Yd = y-(dst.height/2);
						
				//Transformation
					double cosX = Math.cos(rad);
					double sinX = Math.sin(rad);
				double Xs = Xd/(cosX-Xd*s*sinX);
				double Ys = Yd*(s*sinX*Xs+1);
				
				//Translation zuruecksetzen
					double x_n_Wert = Xs+(src.width/2);
					double y_n_Wert = Ys+(src.height/2);
				int x_n = (int)(Math.floor(x_n_Wert)); //math.floor dipake buat dibuletin ke angka bawah
				int y_n = (int)(Math.floor(y_n_Wert));
				
				int pos_n = y_n * src.width + x_n;
				
				if(x_n<src.width && x_n>=0 && y_n<src.height && y_n>=0){
					double h = (x_n_Wert - x_n); //x_n sebelum di abrundet - x_n yg uda di abrundet
					double v = (y_n_Wert - y_n);
					
					int A = src.argb[pos_n];
					if(y_n==0)					//damit nicht treppig
						A = 0xFFFFFFFF;
					float rA = (A >> 16) & 0xff;
					float gA = (A >> 8) & 0xff;								// A B
					float bA = A & 0xff;									// C D
						
					int B;
					if(pos_n+1 < src.argb.length){
						B = src.argb[pos_n + 1];
					if(y_n==0)
						B = 0xFFFFFFFF;
					} else
						B = 0xFFFFFFFF;
					float rB = (B >> 16) & 0xff;
					float gB = (B >> 8) & 0xff;
					float bB = B & 0xff;
					
					int C;
					if(pos_n+src.width< src.argb.length)
						C = src.argb[pos_n + src.width];
					else 
						C = 0xFFFFFFFF;
					float rC = (C >> 16) & 0xff;
					float gC = (C >> 8) & 0xff;
					float bC = C & 0xff;
					
					int D;
					if(pos_n+src.width+1 < src.argb.length)
						D = src.argb[pos_n + src.width + 1];
					else
						D = 0xFFFFFFFF;
					float rD = (D >> 16) & 0xff;
					float gD = (D >> 8) & 0xff;
					float bD = D & 0xff;
					
					int r = (int)(rA*(1-h)*(1-v) + rB*h*(1-v) + rC*(1-h)*v + rD*h*v);
					int g = (int)(gA*(1-h)*(1-v) + gB*h*(1-v) + gC*(1-h)*v + gD*h*v); 
					int b = (int)(bA*(1-h)*(1-v) + bB*h*(1-v) + bC*(1-h)*v + bD*h*v); 
					
					r = Math.min(Math.max(r, 0), 255);
				    g = Math.min(Math.max(g, 0), 255);
				    b = Math.min(Math.max(b, 0), 255);
						
					dst.argb[pos] = (0xFF << 24) | (r << 16) | (g << 8) | b;
				} else {
					dst.argb[pos] = 0xFFFFFFFF;
				}
			}
		}
 	}
}
