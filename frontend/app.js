'use strict';

import { initializeApp } from "firebase/app";

const firebaseConfig = {
  apiKey: "AIzaSyCMFuZvxMWBkIGylcksCkYzE93mI9ZUzAE",
  authDomain: "rayscheduler-2e056.firebaseapp.com",
  projectId: "rayscheduler-2e056",
  storageBucket: "rayscheduler-2e056.firebasestorage.app",
  messagingSenderId: "748720020906",
  appId: "1:748720020906:web:cd755693b369af8a7c9fd3"
};

const app = initializeApp(firebaseConfig);

function datareturn() {
    var studentName = document.getElementById("inputSname").value;
    AddStudents();

}

db.collection("students").add({
    name: studentName,
    surname: studentSurname,
    patronymic: studentPnym,
    grade: studentGrade,
    subject1: studentsubject1,
    subject2: studentsubject2,
    subject3: studentsubject3,
    subject4: studentsubject4,
    subject5: studentsubject5,
    subject6: studentsubject6,
    subject7: studentsubject7,
    subject8: studentsubject8,
    subject9: studentsubject9
})
function addStudent() {
    const student = {
        name: document.getElementById("name").value,
        surname: document.getElementById("surname").value,
        patronymic: document.getElementById("patronymic").value,
        grade: document.getElementById("grade").value,
        subjects: [
            document.getElementById("subject1").value,
            document.getElementById("subject2").value,
            document.getElementById("subject3").value,
            document.getElementById("subject4").value,
            document.getElementById("subject5").value,
            document.getElementById("subject6").value,
            document.getElementById("subject7").value,
            document.getElementById("subject8").value,
            document.getElementById("subject9").value
        ]
    };

    try {
        await addDoc(collection(db, "students"), student);
        alert("Student added successfully!");
    } catch (error) {
        console.error("Error adding student: ", error);
    }
}

    db.collection("teacher").add({
        name: teacherName,
        surname: teacherSurname,
        patronymic: teacherPnym,
        subject1: teachersubject1,
        subject2: teachersubject2,
        subject3: teachersubject3,
        subject4: teachersubject4,
        subject5: teachersubject5,
        subject6: teachersubject6,
        subject7: teachersubject7,
        subject8: teachersubject8,
        subject9: teachersubject9,
        subject10: teachersubject10,
    })
    db.collection("teacheravailability").add({
        day1time: day1time,
        day2time: day2time,
        day3time: day3time,
        day4time: day4time,
        day5time: day5time,
    })

    function addTeacher() {
        const teacher = {
            name: document.getElementById("name").value,
            surname: document.getElementById("surname").value,
            patronymic: document.getElementById("patronymic").value,
            subjects: [
                document.getElementById("subject1").value,
                document.getElementById("subject2").value,
                document.getElementById("subject3").value,
                document.getElementById("subject4").value,
                document.getElementById("subject5").value,
                document.getElementById("subject6").value,
                document.getElementById("subject7").value,
                document.getElementById("subject8").value,
                document.getElementById("subject9").value,
                document.getElementById("subject10").value,
            ],
            
            day1time: document.getElementById("day1time").value,
            day2time: document.getElementById("day2time").value,
            day3time: document.getElementById("day3time").value,
            day4time: document.getElementById("day4time").value,
            day5time: document.getElementById("day5time").value,
            
        };

        try {
            await addDoc(collection(db, "teachers"), teacher);
            alert("Teacher added successfully!");
        } catch (error) {
            console.error("Error adding teacher: ", error);
        }
       
}

    db.collection("subjects").add({
        subjectname: subjectname,
        subjectteacher: subjectteacher,

    })
    function addSubject() {
        const subject = {
            subjectname: document.getElementById("subjectname").value, 
            subjectteacher: document.getElementById("subjectteacher").value,
        }
        try {
            await addDoc(collection(db, "subjects"), subject);
            alert("Subject added successfully!");
        } catch (error) {
            console.error("Error adding subject: ", error);
        }
    }
    db.collection("classroooms").add({
        number: classnumber,
        capacity: classcapacity
    })
    function addClassroom() {
        const classroom = {
            classroomnumber: document.getElementById("classroomnumber").value,
            classcapacity: document.getElementById("classcapacity").value,
            classroomsubject: document.getElementById("classroomsubject").value,
        }
        try {
            await addDoc(collection(db, "classrooms"), classroom);
            alert("Classroom added successfully!");
        } catch (error) {
            console.error("Error adding classroom: ", error);
        }
    }
    function writeSchedule() {

       
    }
function excelExport() {

}