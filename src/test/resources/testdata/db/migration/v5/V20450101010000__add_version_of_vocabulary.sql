
CREATE TABLE if not exists public.vocabulary (
                                 vocabulary_id character varying(20) NOT NULL,
                                 vocabulary_name character varying(255) NOT NULL,
                                 vocabulary_reference character varying(255) NOT NULL,
                                 vocabulary_version character varying(255),
                                 vocabulary_concept_id integer NOT NULL
);

COPY public.vocabulary (vocabulary_id, vocabulary_name, vocabulary_reference, vocabulary_version, vocabulary_concept_id) FROM stdin;
ABMS	Provider Specialty (American Board of Medical Specialties)	http://www.abms.org/member-boards/specialty-subspecialty-certificates	2018-06-26 ABMS	45756746
AMT	Australian Medicines Terminology (NEHTA)	https://www.nehta.gov.au/implementation-resources/terminology-access	AMT 01-SEP-17	238
APC	Ambulatory Payment Classification (CMS)	http://www.cms.gov/Medicare/Medicare-Fee-for-Service-Payment/HospitalOutpatientPPS/Hospital-Outpatient-Regulations-and-Notices.html	2018-January-Addendum-A	44819132
ATC	WHO Anatomic Therapeutic Chemical Classification	FDB UK distribution package	RXNORM 2018-08-12	44819117
BDPM	Public Database of Medications (Social-Sante)	http://base-donnees-publique.medicaments.gouv.fr/telechargement.php	BDPM 20191006	236
CDM	OMOP Common DataModel	https://github.com/OHDSI/CommonDataModel	CDM v6.0.0	32485
CIEL	Columbia International eHealth Laboratory (Columbia University)	https://wiki.openmrs.org/display/docs/Getting+and+Using+the+MVP-CIEL+Concept+Dictionary	Openmrs 1.11.0 20150227	45905710
CMS Place of Service	Place of Service Codes for Professional Claims (CMS)	http://www.cms.gov/Medicare/Medicare-Fee-for-Service-Payment/PhysicianFeeSched/downloads//Website_POS_database.pdf	2009-01-11	44819110
Cohort	Legacy OMOP HOI or DOI cohort	OMOP generated	\N	44819123
Cohort Type	OMOP Cohort Type	OMOP generated	\N	44819234
Concept Class	OMOP Concept Class	OMOP generated	\N	44819233
Condition Type	OMOP Condition Occurrence Type	OMOP generated	\N	44819127
Cost	OMOP Cost	OMOP generated	\N	581457
Cost Type	OMOP Cost Type	OMOP generated	\N	5029
CPT4	Current Procedural Terminology version 4 (AMA)	http://www.nlm.nih.gov/research/umls/licensedcontent/umlsknowledgesources.html	2020 Release	44819100
CTD	Comparative Toxicogenomic Database	http://ctdbase.org	CTD 2020-02-19	32735
Currency	International Currency Symbol (ISO 4217)	http://www.iso.org/iso/home/standards/currency_codes.htm	2008	44819153
CVX	CDC Vaccine Administered CVX (NCIRD)	https://www2a.cdc.gov/vaccines/iis/iisstandards/vaccines.asp?rpt=cvx	CVX Code Set 20180831	581400
Death Type	OMOP Death Type	OMOP generated	\N	44819135
Device Type	OMOP Device Type	OMOP generated	\N	44819151
dm+d	Dictionary of Medicines and Devices (NHS)	https://isd.hscic.gov.uk/trud3/user/authenticated/group/0/pack/1/subpack/24/releases	dm+d Version 1.4.0 20190128	232
Domain	OMOP Domain	OMOP generated	\N	44819147
DPD	Drug Product Database (Health Canada)	http://open.canada.ca/data/en/dataset/bf55e42a-63cb-4556-bfd8-44f26e5a36fe	DPD 25-JUN-17	231
DRG	Diagnosis-related group (CMS)	http://www.cms.gov/Medicare/Medicare-Fee-for-Service-Payment/AcuteInpatientPPS/Acute-Inpatient-Files-for-Download.html	2011-18-02	44819130
Drug Type	OMOP Drug Exposure Type	OMOP generated	\N	44819126
EDI	Korean EDI	http://www.hira.or.kr/rd/insuadtcrtr/bbsView.do?pgmid=HIRAA030069000400&brdScnBltNo=4&brdBltNo=51354&pageIndex=1&isPopupYn=Y	EDI 2019.10.01	32736
EphMRA ATC	Anatomical Classification of Pharmaceutical Products (EphMRA)	http://www.ephmra.org/Anatomical-Classification	EphMRA ATC 2016	243
Episode	OMOP Episode	OMOP generated	\N	32523
Episode Type	OMOP Episode Type	OMOP generated	\N	32542
Ethnicity	OMOP Ethnicity	OMOP generated	\N	44819134
GCN_SEQNO	Clinical Formulation ID (FDB)	FDB US distribution package	20151119 Release	44819141
Gender	OMOP Gender	OMOP generated	\N	44819108
GGR	Commented Drug Directory (BCFI)	http://www.bcfi.be/nl/download	GGR 20200401	581450
GPI	Medi-Span Generic Product Identifier (Wolters Kluwer Health)	Medi-Span distribution package	GPI 2017	44819106
HCPCS	Healthcare Common Procedure Coding System (CMS)	http://www.nlm.nih.gov/research/umls/licensedcontent/umlsknowledgesources.html	2020 Alpha Numeric HCPCS File	44819101
HemOnc	HemOnc	https://hemonc.org	HemOnc 2019-08-29	32613
HES Specialty	Hospital Episode Statistics Specialty (NHS)	http://www.datadictionary.nhs.uk/data_dictionary/attributes/m/main_specialty_code_de.asp?shownav=0	2018-06-26 HES Specialty	44819145
ICD10	International Classification of Diseases, Tenth Revision (WHO)	http://www.who.int/classifications/icd/icdonlineversions/en/	2016 Release	44819124
ICD10CM	International Classification of Diseases, Tenth Revision, Clinical Modification (NCHS)	http://www.cdc.gov/nchs/icd/icd10cm.htm	ICD10CM FY2020 code descriptions	44819098
ICD10CN	International Classification of Diseases, Tenth Revision, Chinese Edition	http://www.sac.gov.cn/was5/web/search?channelid=97779&templet=gjcxjg_detail.jsp&searchword=STANDARD_CODE=%27GB/T%2014396-2016%27	2016 Release	32740
ICD10PCS	ICD-10 Procedure Coding System (CMS)	http://www.cms.gov/Medicare/Coding/ICD10/index.html	ICD10PCS 2020	44819125
ICD9CM	International Classification of Diseases, Ninth Revision, Clinical Modification, Volume 1 and 2 (NCHS)	http://www.cms.gov/Medicare/Coding/ICD9ProviderDiagnosticCodes/codes.html	ICD9CM v32 master descriptions	5046
ICD9Proc	International Classification of Diseases, Ninth Revision, Clinical Modification, Volume 3 (NCHS)	http://www.cms.gov/Medicare/Coding/ICD9ProviderDiagnosticCodes/codes.html	ICD9CM v32 master descriptions	44819099
ICD9ProcCN	International Classification of Diseases, Ninth Revision, Chinese Edition, Procedures	http://chiss.org.cn/hism/wcmpub/hism1029/notice/201712/P020171225613285104950.pdf	2017 Release	32744
ICDO3	International Classification of Diseases for Oncology, Third Edition (WHO)	https://seer.cancer.gov/icd-o-3/	ICDO3 SEER Site/Histology Released 06/2019	581426
JMDC	Japan Medical Data Center Drug Code (JMDC)	OMOP generated	JMDC 2019-03-11	32557
KCD7	Korean Classification of Diseases, 7th Revision	https://www.hira.or.kr/rd/insuadtcrtr/bbsView.do?pgmid=HIRAA030069000000&brdScnBltNo=4&brdBltNo=50760&pageIndex=1&isPopupYn=Y#none	7th revision	32688
KNHIS	Korean National Health Information System	OMOP generated	\N	32723
Korean Revenue Code	Korean Revenue Code	OMOP generated	\N	32724
LOINC	Logical Observation Identifiers Names and Codes (Regenstrief Institute)	http://loinc.org/downloads/loinc	2.67	44819102
MDC	Major Diagnostic Categories (CMS)	http://www.cms.gov/Medicare/Medicare-Fee-for-Service-Payment/AcuteInpatientPPS/Acute-Inpatient-Files-for-Download.html	2013-01-06	44819131
Meas Type	OMOP Measurement Type	OMOP generated	\N	44819152
MedDRA	Medical Dictionary for Regulatory Activities (MSSO)	http://www.meddramsso.com/subscriber_download.asp	MedDRA Version 22.0	44819111
Medicare Specialty	Medicare provider/supplier specialty codes (CMS)	http://www.cms.gov/Medicare/Provider-Enrollment-and-Certification/MedicareProviderSupEnroll/Taxonomy.html	2018-06-26 Specialty	44819138
MEDRT	Medication Reference Terminology MED-RT (VA)	https://nciterms.nci.nih.gov/ncitbrowser/pages/vocabulary.jsf?dictionary=MED-RT	MED-RT 2018-09-04	32537
MeSH	Medical Subject Headings (NLM)	http://www.nlm.nih.gov/research/umls/licensedcontent/umlsknowledgesources.html	2020 Release	44819136
Metadata	Metadata	OMOP generated	\N	32675
MMI	Modernizing Medicine (MMI)	MMI proprietary	\N	581367
Multum	Cerner Multum (Cerner)	http://www.nlm.nih.gov/research/umls/rxnorm/docs/rxnormfiles.html	2013-07-10	44819112
NAACCR	NAACCR Data Dictionary	http://datadictionary.naaccr.org/?c=10	NAACCR v18	32642
NDC	National Drug Code (FDA and manufacturers)	http://www.nlm.nih.gov/research/umls/rxnorm/docs/rxnormfiles.html, http://www.fda.gov/downloads/Drugs/DevelopmentApprovalProcess/UCM070838.zip	NDC 20200517	44819105
NDFRT	National Drug File - Reference Terminology (VA)	http://www.nlm.nih.gov/research/umls/rxnorm/docs/rxnormfiles.html	RXNORM 2018-08-12	44819103
Nebraska Lexicon	Nebraska Lexicon	https://www.unmc.edu/pathology-research/bioinformatics/campbell/tdc.html	Nebraska Lexicon 20190816	32757
NFC	New Form Code (EphMRA)	http://www.ephmra.org/New-Form-Codes-Classification	NFC 20160704	245
None	OMOP Standardized Vocabularies	OMOP generated	v5.0 19-MAY-20	44819096
Note Type	OMOP Note Type	OMOP generated	\N	44819146
NUCC	National Uniform Claim Committee Health Care Provider Taxonomy Code Set (NUCC)	http://www.nucc.org/index.php?option=com_content&view=article&id=107&Itemid=132	2018-06-26 NUCC	44819137
Observation Type	OMOP Observation Type	OMOP generated	\N	44819129
Obs Period Type	OMOP Observation Period Type	OMOP generated	\N	44819149
OMOP Extension	OMOP Extension	OMOP generated	OMOP Extension 20200518	32758
OPCS4	OPCS Classification of Interventions and Procedures version 4 (NHS)	http://systems.hscic.gov.uk/data/clinicalcoding/codingstandards/opcs4	2019 Release	44819143
OSM	OpenStreetMap	https://www.openstreetmap.org/copyright/en, https://wambachers-osm.website/boundaries/	OSM Release 2019-02-21	32541
OXMIS	Oxford Medical Information System (OCHP)	Codes extracted from GPRD database, courtesy of GSK	\N	44819114
PCORNet	National Patient-Centered Clinical Research Network (PCORI)	OMOP generated	\N	44819148
PHDSC	Source of Payment Typology (PHDSC)	http://www.phdsc.org/standards/payer-typology-source.asp	Version 3.0	32473
Plan	Health Plan - contract to administer healthcare transactions by the payer, facilitated by the sponsor	OMOP generated	\N	32471
Plan Stop Reason	Plan Stop Reason - Reason for termination of the Health Plan	OMOP generated	\N	32474
PPI	AllOfUs_PPI (Columbia)	http://terminology.pmi-ops.org/CodeSystem/ppi	Codebook Version 0.3.34	581404
Procedure Type	OMOP Procedure Occurrence Type	OMOP generated	\N	44819128
Provider	OMOP Provider	OMOP generated	\N	32573
Race	Race and Ethnicity Code Set (USBC)	http://www.cdc.gov/nchs/data/dvs/Race_Ethnicity_CodeSet.pdf	Version 1.0	44819109
Read	NHS UK Read Codes Version 2 (HSCIC)	http://www.nlm.nih.gov/research/umls/licensedcontent/umlsknowledgesources.html	NHS READV2 21.0.0 20160401000001 + DATAMIGRATION_25.0.0_20180403000001	44819113
Relationship	OMOP Relationship	OMOP generated	\N	44819235
Revenue Code	UB04/CMS1450 Revenue Codes (CMS)	http://www.mpca.net/?page=ERC_finance	2010 Release	44819133
RxNorm	RxNorm (NLM)	http://www.nlm.nih.gov/research/umls/rxnorm/docs/rxnormfiles.html	RxNorm 20190903	44819104
RxNorm Extension	RxNorm Extension (OMOP)	OMOP generated	RxNorm Extension 2020-04-30	252
SMQ	Standardised MedDRA Queries (MSSO)	http://www.meddramsso.com/secure/smq/SMQ_Spreadsheet_14_0_English_update.xlsx	Version 14.0	44819121
SNOMED	Systematic Nomenclature of Medicine - Clinical Terms (IHTSDO)	http://www.nlm.nih.gov/research/umls/licensedcontent/umlsknowledgesources.html	SNOMED 2020-04-01	44819097
SNOMED Veterinary	SNOMED Veterinary	https://vtsl.vetmed.vt.edu/extension/	SNOMED Veterinary 20190401	32549
Specimen Type	OMOP Specimen Type	OMOP generated	\N	581376
SPL	Structured Product Labeling (FDA)	http://www.fda.gov/Drugs/InformationOnDrugs/ucm142438.htm	NDC 20200517	44819140
Sponsor	Sponsor - institution or individual financing healthcare transactions	OMOP generated	\N	32472
Supplier	OMOP Supplier	OMOP generated	\N	32574
UB04 Point of Origin	UB04 Claim Source Inpatient Admission Code (CMS)	https://www.resdac.org/cms-data/variables/Claim-Source-Inpatient-Admission-Code	\N	32045
UB04 Pri Typ of Adm	UB04 Claim Inpatient Admission Type Code (CMS)	https://www.resdac.org/cms-data/variables/Claim-Inpatient-Admission-Type-Code	\N	32046
UB04 Pt dis status	UB04 Patient Discharge Status Code (CMS)	https://www.resdac.org/cms-data/variables/patient-discharge-status-code	\N	32047
UB04 Typ bill	UB04 Type of Bill - Institutional (USHIK)	https://ushik.ahrq.gov/ViewItemDetails?&system=apcd&itemKey=196987000	\N	32044
UCUM	Unified Code for Units of Measure (Regenstrief Institute)	http://aurora.regenstrief.org/~ucum/ucum.html#section-Alphabetic-Index	Version 1.8.2	44819107
US Census	United States Census Bureau	https://www.census.gov/geo/maps-data/data/tiger-cart-boundary.html	US Census 2017 Release	32570
VA Class	VA National Drug File Class (VA)	http://www.nlm.nih.gov/research/umls/rxnorm/docs/rxnormfiles.html	RXNORM 2018-08-12	44819122
VA Product	VA National Drug File Product (VA)	http://www.nlm.nih.gov/research/umls/rxnorm/docs/rxnormfiles.html	RXNORM 2018-08-12	44819120
Visit	OMOP Visit	OMOP generated	\N	44819119
Visit Type	OMOP Visit Type	OMOP generated	\N	44819150
Vocabulary	OMOP Vocabulary	OMOP generated	\N	44819232
\.
