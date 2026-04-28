# 🌐 CyberSense: Problematic Internet Use Predictor

## 📖 Overview
This project is an interactive data science web application built in **R and Shiny** to predict a child's **Internet Usage Score** (Problematic Internet Use) and classify the associated risk levels (Low, Moderate, or High). It leverages machine learning algorithms, specifically a Random Forest model, to assess the likelihood of internet addiction based on demographics, daily internet usage, and the Parent-Child Internet Addiction Test (PCIAT) responses.

## ✨ Key Features
- **Predictive Modeling**: Utilizes a trained Random Forest model (`random_forest_model.rds`) to estimate the PCIAT total score based on user inputs.
- **Interactive Shiny Web App**: A fully functional, user-friendly interface with three main panels:
  - **Demo-Graphics**: Inputs for Age, Internet Hours, and SDS Total T Score.
  - **PCIAT Questionnaire**: 20 intuitive inputs corresponding to the Parent-Child Internet Addiction Test.
  - **Prediction Dashboard**: Generates the final score, visually represented via a custom interactive Plotly gauge meter.
- **Risk Assessment**: Categorizes predictions into:
  - 🧠 **Low Risk** (Score 0-40)
  - ⚡ **Moderate Risk** (Score 41-70)
  - 🚨 **High Risk** (Score 71-100)
- **Data Pipeline Integration**: Contains comprehensive R scripts for data preprocessing, outlier handling, feature engineering, and model training.

## 🛠️ Technology Stack
- **Language**: R
- **Web Framework**: Shiny (`shiny`, `shinycssloaders`, `shinyalert`)
- **Machine Learning**: `randomForest`
- **Data Visualization**: `plotly`
- **Data Manipulation**: `dplyr`

## 👥 Contributors
This project was successfully developed by:
- **Aman** 
- **Hiral**

**Key Contributions include:**
- **Data Preprocessing & Engineering:** Cleaned raw survey datasets, handled outliers, normalized values, and extracted the most significant predictors (Age, Internet Hours, SDS Score, and 20 PCIAT questions).
- **Model Training & Tuning:** Built, evaluated, and tuned robust machine learning models (including Random Forest and SVM) to ensure accurate and reliable predictions.
- **UI/UX Design & Deployment:** Developed a highly interactive, responsive, and visually appealing Shiny application with custom CSS styling, dynamic loaders, pop-up alerts, and Plotly gauge charts to present findings seamlessly to end-users.

## 📂 Repository Structure
- `App_Final.R`: The main, stylized Shiny application file.
- `Preprocessing_Data.R` / `FeatureEngg.R` / `Cleaning_Outlier_Handling_SplitTrain&Test.R`: Scripts detailing the data cleaning, feature selection, and transformation pipeline.
- `ModelTraining.R` / `Train_Test_Tune.R`: Scripts for training the machine learning models.
- `random_forest_model.rds` / `final_svm_model.rds`: Serialized predictive models ready for production.

---
*Created as part of our Data Science Project.*
