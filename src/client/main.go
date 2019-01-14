package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"math/rand"
	"net/http"
	"os"
	"sync"
	"time"
)

type Response struct {
	Provincias []Provincia `json:"provincias"`
}

type Provincia struct {
	Nombre      string      `json:"nombre"`
	Poblaciones []Poblacion `json:"poblaciones"`
}

type Poblacion struct {
	Nombre      string `json:"nombre"`
	NumColegios int    `json:"colegiosElectorales"`
	Censo       int    `json:"censo"`
}

type Recuento struct {
	NombreProvincia string         `json:"provincia"`
	NombrePoblacion string         `json:"poblacion"`
	NumColegio      int            `json:"numColegio"`
	VotosPartidos   []VotosPartido `json:"votos"`
	Participacion   int            `json:"participacion"`
}

type VotosPartido struct {
	NombrePartido string `json:"partido"`
	Votos         int    `json:"votos"`
}

var partidos = [...]string{
	"Frente Popular de Judea",
	"Frente Judaico Popular",
	"Accion Votante",
	"Union de Cinturones Apretados",
	"Partido Socialista Popular"}

const HOSTNAME string = "localhost"
const PORT string = "8080"

func main() {

	response, err := http.Get("http://" + HOSTNAME + ":" + PORT + "/api/demographics")
	if err != nil {
		fmt.Print(err.Error())
		os.Exit(1)
	}

	responseData, err := ioutil.ReadAll(response.Body)
	if err != nil {
		log.Fatal(err)
	}

	var responseObject Response
	json.Unmarshal(responseData, &responseObject)

	var wg sync.WaitGroup
	for _, provincia := range responseObject.Provincias {
		wg.Add(1)
		go procesaProvincia(provincia, &wg)
	}

	wg.Wait()
	log.Println("Todos los datos enviados")
}

func procesaProvincia(provincia Provincia, wgProv *sync.WaitGroup) {
	defer wgProv.Done()
	var wg sync.WaitGroup
	for _, poblacion := range provincia.Poblaciones {
		wg.Add(1)
		go procesaPoblacion(poblacion, provincia.Nombre, &wg)
	}
	wg.Wait()
	log.Println("Procesada " + provincia.Nombre)
}

func procesaPoblacion(poblacion Poblacion, nombreProvincia string, wgPob *sync.WaitGroup) {
	defer wgPob.Done()

	var wg sync.WaitGroup
	censo := poblacion.Censo
	var sumaVotosTime, partidosVotados int
	maxVotosColegio := poblacion.Censo / poblacion.NumColegios

	rand.Seed(time.Now().UnixNano())
	for numColegio := 0; numColegio < poblacion.NumColegios; numColegio++ {
		wg.Add(1)
		if censo < 500 {
			sumaVotosTime = rand.Intn(5) + 10
			partidosVotados = rand.Intn(1) + 2
		} else if censo >= 500 && censo < 5000 {
			sumaVotosTime = rand.Intn(15) + 15
			partidosVotados = 3
		} else if censo >= 5000 && censo < 25000 {
			sumaVotosTime = rand.Intn(15) + 30
			partidosVotados = 3
		} else if censo >= 25000 && censo < 100000 {
			sumaVotosTime = rand.Intn(15) + 45
			partidosVotados = rand.Intn(1) + 4
		} else if censo >= 100000 && censo < 500000 {
			sumaVotosTime = rand.Intn(15) + 60
			partidosVotados = rand.Intn(1) + 4
		} else {
			sumaVotosTime = rand.Intn(15) + 75
			partidosVotados = rand.Intn(1) + 4
		}
		go procesaColegio(nombreProvincia, poblacion.Nombre, numColegio, sumaVotosTime, maxVotosColegio, partidosVotados, &wg)
	}
	wg.Wait()
}

func procesaColegio(nombreProvincia string, nombrePoblacion string, numColegio int,
	sumaVotosTime int, maxVotosColegio int, partidosVotados int, wgCol *sync.WaitGroup) {

	defer wgCol.Done()

	//Sleep for the given time to simulate votes recount
	time.Sleep(time.Duration(sumaVotosTime) * time.Second)

	participMin := rand.Intn(20) + 30
	participMax := rand.Intn(20) + 55
	particip := rand.Intn(participMax-participMin) + participMin
	maxVotosPct := 100

	resultColegio := Recuento{NombreProvincia: nombreProvincia, NombrePoblacion: nombrePoblacion, NumColegio: numColegio, Participacion: particip}

	for index := 0; index < partidosVotados && maxVotosPct > 1; index++ {
		var pctVotos int
		if index+1 == partidosVotados {
			pctVotos = maxVotosPct
		} else {
			pctVotos = rand.Intn(maxVotosPct-1) + 1
		}
		votos := (maxVotosColegio * pctVotos * particip) / 10000
		votosPartido := VotosPartido{NombrePartido: partidos[index], Votos: votos}
		resultColegio.VotosPartidos = append(resultColegio.VotosPartidos, votosPartido)
		maxVotosPct = maxVotosPct - pctVotos
	}

	jsonValue, _ := json.Marshal(resultColegio)
	_, err := http.Post("http://"+HOSTNAME+":"+PORT+"/api/recuento", "application/json", bytes.NewBuffer(jsonValue))
	if err != nil {
		fmt.Print(err.Error())
		os.Exit(1)
	}

}
