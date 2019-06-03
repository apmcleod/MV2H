#ifndef BasicCalculation_HPP
#define BasicCalculation_HPP

#include<iostream>
#include<fstream>
#include<string>
#include<sstream>
#include<vector>
#include<stdio.h>
#include<stdlib.h>
#include<cmath>
#include<cassert>
#include<algorithm>
using namespace std;

inline int gcd(int m, int n){
	if(0==m||0==n){return 0;}
	while(m!=n){if(m>n){m=m-n;}else{n=n-m;}}//endwhile
	return m;
}//end gcd
inline int lcm(int m,int n){
	if (0==m||0==n){return 0;}
	return ((m/gcd(m,n))*n);//lcm=m*n/gcd(m,n)
}//end lcm

inline double LogAdd(double d1,double d2){
	//log(exp(d1)+exp(d2))=log(exp(d1)(1+exp(d2-d1)))
	if(d1>d2){
//		if(d1-d2>20){return d1;}
		return d1+log(1+exp(d2-d1));
	}else{
//		if(d2-d1>20){return d2;}
		return d2+log(1+exp(d1-d2));
	}//endif
}//end LogAdd
inline void Norm(vector<double>& vd){
	double sum=0;
	for(int i=0;i<vd.size();i+=1){
		sum+=vd[i];
//		if(vd[i]<0){cout<<"negative weight!"<<endl;}
	}//endif
	for(int i=0;i<vd.size();i+=1){vd[i]/=sum;}
	return;
}//end Norm
inline void Lognorm(vector<double>& vd){
	double tmpd=vd[0];
	for(int i=0;i<vd.size();i+=1){if(vd[i]>tmpd){tmpd=vd[i];}}//endfor i
	for(int i=0;i<vd.size();i+=1){vd[i]-=tmpd;}//endfor i
	tmpd=0;
	for(int i=0;i<vd.size();i+=1){tmpd+=exp(vd[i]);}//endfor i
	tmpd=log(tmpd);
	for(int i=0;i<vd.size();i+=1){vd[i]-=tmpd;if(vd[i]<-200){vd[i]=-200;}}//endfor i
}//end Lognorm

inline double Average(vector<double>& vd){
	assert(vd.size()>0);
	double sum=0;
	for(int i=0;i<vd.size();i+=1){
		sum+=vd[i];
	}//endfor i
	return sum/double(vd.size());
}//end Average

inline double StDev(vector<double>& vd){
	assert(vd.size()>1);
	double ave=Average(vd);
	double sum=0;
	for(int i=0;i<vd.size();i+=1){
		sum+=pow(vd[i]-ave,2.);
	}//endfor i
	return pow(sum/double(vd.size()-1),0.5);
}//end StDev

double KLDiv(vector<double> p,vector<double> q,double regularizer=0){//p given q
	assert(p.size()==q.size());
	double sum=0;
	for(int i=0;i<p.size();i+=1){
		if(p[i]<1E-100){continue;}
		sum+=p[i]*(log(p[i])-log(q[i]+regularizer));
	}//endfor p
	return sum;
}//end KLDiv

double SqDist(vector<double> p,vector<double> q,double scale=1){//p given q
	assert(p.size()==q.size());
	double sum=0;
	for(int i=0;i<p.size();i+=1){
		sum+=pow((p[i]-q[i])/scale,2.);
	}//endfor p
	return sum;
}//end SqDist

inline int SampleDistr(vector<double> &p){
	double val=(1.0*rand())/(1.0*RAND_MAX);
	for(int i=0;i<p.size()-1;i+=1){
		if(val<p[i]){return i;
		}else{val-=p[i];
		}//endif
	}//endfor i
	return p.size()-1;
}//end SampleDistr

inline double RandDouble(){
	return (1.0*rand())/(1.0*RAND_MAX);
}//end

inline double RandDoubleInRange(double from,double to){
	return (1.0*rand())/(1.0*RAND_MAX)*(to-from)+from;
}//end

inline double SampleGauss(double mu,double stdev){
	double x,y;
	x=(1.*rand())/(1.*RAND_MAX);
	y=(1.*rand())/(1.*RAND_MAX);
	return sqrt(-1*log(x))*cos(2*M_PI*y)*stdev+mu;
}//end

class Pair{
public:
	int ID;
	double value;
};//endclass Pair

class MorePair{
public:
	bool operator()(const Pair& a, const Pair& b){
		if(a.value > b.value){
			return true;
		}else{//if a.value <= b.value
			return false;
		}//endif
	}//end operator()
};//end class MorePair
//sort(pairs.begin(), pairs.end(), MorePair());

inline vector<double> Intervals(double valmin,double valmax,int nPoint){
	vector<double> values;
	double eps=(valmax-valmin)/double(nPoint-1);
	for(int i=0;i<nPoint;i+=1){
		values.push_back( valmin+i*eps );
	}//endfor i
	return values;
}//end Intervals

inline vector<double> LogIntervals(double valmin,double valmax,int nPoint){
	vector<double> values;
	double eps=(log(valmax)-log(valmin))/double(nPoint-1);
	for(int i=0;i<nPoint;i+=1){
		values.push_back( valmin*exp(i*eps) );
	}//endfor i
	return values;
}//end LogIntervals

//From Prob_v160925.hpp
template <typename T> class Prob{
public:
	vector<double> P;
	vector<double> LP;
	vector<T> samples;

	Prob(){
	}//end Prob
	Prob(Prob<T> const & prob_){
		P=prob_.P;
		LP=prob_.LP;
		samples=prob_.samples;
	}//end Prob

	~Prob(){
	}//end ~Prob

	Prob& operator=(const Prob<T> & prob_){
		P=prob_.P;
		LP=prob_.LP;
		samples=prob_.samples;
		return *this;
	}//end =

	void Print(){
		for(int i=0;i<P.size();i+=1){
cout<<i<<"\t"<<samples[i]<<"\t"<<P[i]<<"\t"<<LP[i]<<endl;
		}//endfor i
	}//end Print

	void Normalize(){
		Norm(P);
		PToLP();
	}//end Normalize

	void LogNormalize(){
		Lognorm(LP);
		LPToP();
	}//end Normalize

	void PToLP(){
		LP.clear();
		LP.resize(P.size());
		for(int i=0;i<P.size();i+=1){
			LP[i]=log(P[i]);
		}//endfor i
	}//end PToLP

	void LPToP(){
		P.clear();
		P.resize(LP.size());
		for(int i=0;i<LP.size();i+=1){
			P[i]=exp(LP[i]);
		}//endfor i
	}//end LPToP

	T Sample(){
		return samples[SampleDistr(P)];
	}//end Sample

	void Clear(){
		P.clear(); LP.clear(); samples.clear();
	}//end Clear

	void Resize(int _size){
		P.clear(); LP.clear(); samples.clear();
		P.resize(_size);
		LP.resize(_size);
		samples.resize(_size);
	}//end Resize

	void Assign(int _size,double value){
		P.clear(); LP.clear(); samples.clear();
		P.assign(_size,value);
		LP.resize(_size);
		samples.resize(_size);
	}//end Assign

	double MaxP(){
		double max=P[0];
		for(int i=1;i<P.size();i+=1){
			if(P[i]>max){max=P[i];}
		}//endfor i
		return max;
	}//end MaxValue

	int ModeID(){
		double max=P[0];
		int modeID=0;
		for(int i=1;i<P.size();i+=1){
			if(P[i]>max){modeID=i;}
		}//endfor i
		return modeID;
	}//end ModeID

	void Randomize(){
		for(int i=0;i<P.size();i+=1){
			P[i]=(1.0*rand())/(1.0*RAND_MAX);
		}//endfor i
		Normalize();
	}//end Randomize

	void Sort(){
		vector<Pair> pairs;
		Pair pair;
		for(int i=0;i<P.size();i+=1){
			pair.ID=i;
			pair.value=P[i];
			pairs.push_back(pair);
		}//endfor i
		stable_sort(pairs.begin(), pairs.end(), MorePair());

		Prob<T> tmpProb;
		tmpProb=*this;
		for(int i=0;i<P.size();i+=1){
			P[i]=tmpProb.P[pairs[i].ID];
			samples[i]=tmpProb.samples[pairs[i].ID];
		}//endfor i
		PToLP();

	}//end Sort

	double Entropy(){
		double ent=0;
		for(int i=0;i<P.size();i+=1){
			if(P[i]<1E-10){continue;}
			ent+=-P[i]*log(P[i]);
		}//endfor i
		return ent;
	}//end Entropy

};//endclass Prob



class TemporalDataSample{
public:
	string label;
	double time;
	int dimValue;
	vector<double> values;
};//endclass TemporalSample

class TemporalData{
public:
	vector<int> refTimes;//E.g. 1900,2000 => intervals are (-inf,1900) [1900,2000) [2000,inf)
	vector<TemporalDataSample> data;
	vector<vector<vector<double> > > statistics;//(refYears.size+1)xdimValuex3; #samples,mean,stdev
	int dimValue;

	void PrintTimeIntervals(){
		cout<<"(-inf,"<<refTimes[0]<<")"<<endl;
		for(int i=1;i<refTimes.size();i+=1){
			cout<<"["<<refTimes[i-1]<<","<<refTimes[i]<<")"<<endl;
		}//endfor i
		cout<<"["<<refTimes[refTimes.size()-1]<<",inf)"<<endl;
	}//end PrintTimeIntervals

	void PrintStatistics(){
		cout<<"#(-inf,"<<refTimes[0]<<")"<<"\t"<<refTimes[0]<<"\t"<<statistics[0][0][0];
		for(int k=0;k<statistics[0].size();k+=1){
			cout<<"\t"<<statistics[0][k][1]<<"\t"<<statistics[0][k][2];
		}//endfor k
		cout<<endl;
		for(int i=1;i<refTimes.size();i+=1){
			cout<<"["<<refTimes[i-1]<<","<<refTimes[i]<<")"<<"\t"<<0.5*(refTimes[i-1]+refTimes[i])<<"\t"<<statistics[i][0][0];
			for(int k=0;k<statistics[0].size();k+=1){
				cout<<"\t"<<statistics[i][k][1]<<"\t"<<statistics[i][k][2];
			}//endfor k
			cout<<endl;
		}//endfor i
		cout<<"#["<<refTimes[refTimes.size()-1]<<",inf)"<<"\t"<<refTimes[refTimes.size()-1]<<"\t"<<statistics[refTimes.size()][0][0];
		for(int k=0;k<statistics[0].size();k+=1){
			cout<<"\t"<<statistics[refTimes.size()][k][1]<<"\t"<<statistics[refTimes.size()][k][2];
		}//endfor k
		cout<<endl;
	}//end PrintStatistics

	void AddDataSample(TemporalDataSample sample){
		data.push_back(sample);
	}//end AddDataSample

	void Analyze(){
		vector<vector<vector<double> > > values;
		values.resize(refTimes.size()+1);
		int timeID;
		for(int n=0;n<data.size();n+=1){
			timeID=0;
			for(int i=0;i<refTimes.size();i+=1){
				if(data[n].time>=refTimes[i]){timeID=i+1;
				}else{break;
				}//endif
			}//endfor i
			values[timeID].push_back(data[n].values);
		}//endfor n
		dimValue=data[0].dimValue;

		statistics.clear();
		statistics.resize(refTimes.size()+1);
		for(int i=0;i<statistics.size();i+=1){
			statistics[i].resize(dimValue);
			for(int k=0;k<dimValue;k+=1){
				statistics[i][k].resize(3);
				vector<double> vd;
				for(int n=0;n<values[i].size();n+=1){
					vd.push_back(values[i][n][k]);
				}//endfor n
				statistics[i][k][0]=vd.size();
				if(statistics[i][k][0]==0){
				}else if(statistics[i][k][0]==1){
					statistics[i][k][1]=vd[0];
					statistics[i][k][2]=0;
				}else{
					statistics[i][k][1]=Average(vd);
					statistics[i][k][2]=StDev(vd);
				}//endif

			}//endfor k
		}//endfor i

	}//end Analyze

};//endclass TemporalData



#endif // BasicCalculation_HPP
