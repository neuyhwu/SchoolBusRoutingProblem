package algorithm;

import datareader.DataReader;
import distance.*;
import model.Bus;
import model.Pupil;
import model.School;

import java.util.*;

public class Algorithm {

    private List<Bus> buses;
    private List<Pupil> pupils;
    private List<School> schools;

    private Map<Pupil, List<PupilDistance>> distanceBetweenPupil;

    private Map<Bus, List<BusDistance>> distanceBetweenBusesAndPupils;

    private Map<School, List<SchoolDistance>> distanceBetweenSchoolsAndPupils;

    private List<Route> minimalRoute = new ArrayList<>();

    private List<Route> globalRoute = new ArrayList<>();



    public void init () {
        getData();
        countDistance();
    }

    private void getData() {
        DataReader dataReader = new DataReader();

        buses = dataReader.getBuses();
        pupils = dataReader.getPupils();
        schools = dataReader.getSchools();

        Bus.printBuses(buses);
        Pupil.printPupils(pupils);
        School.printSchools(schools);
    }

    private void countDistance () {
        CountDistance distance = new CountDistance(schools, pupils, buses);

        distanceBetweenPupil = distance.getDistanceBetweenPupils();
        distanceBetweenBusesAndPupils = distance.getDistanceBetweenBusesAndPupils();
        distanceBetweenSchoolsAndPupils = distance.getDistanceBetweenSchoolsAndPupils();

        distance.printDistances();
        distance.printBusDistance();
        distance.printSchoolDistance();

        busToSchool();
        printMinimalRoute();
    }

    private void busToSchool(){

        while (distanceBetweenBusesAndPupils.size() > 0) {
            for (Map.Entry<Bus, List<BusDistance>> entry: distanceBetweenBusesAndPupils.entrySet()) {
                routeBusToSchool(entry.getValue(), entry.getKey());
            }
            removeElements();
        }

    }

    private Route findMinimalRoute() {
        Route minRoute = new Route();
        for (Route route : minimalRoute) {
            if (minRoute.getDistance() > route.getDistance()) {
                minRoute = route;
            }
        }
        return minRoute;
    }
    private void removeElements() {
        Route minRoute = findMinimalRoute();

        removePupils(minRoute);
        removeBuses(minRoute);
        removeSchools(minRoute);
        removeRoute(minRoute);
    }

    private void removeSchools(Route minRoute) {
        distanceBetweenSchoolsAndPupils.remove(minRoute.getSchool());
    }

    private void removeBuses(Route minRoute) {
        distanceBetweenBusesAndPupils.remove(minRoute.getBus());
    }


    private void removePupils(Route minRoute) {

        for (Pupil pupil : minRoute.getBusRoute()) {
            distanceBetweenPupil.remove(pupil);
        }
    }

    private void removeRoute(Route minRoute) {
        minimalRoute.clear();
        globalRoute.add(minRoute);
    }


    private void routeBusToSchool(List<BusDistance> busDistance, Bus bus) {

        Route minRoute = new Route();
        for (Map.Entry<School, List<SchoolDistance>> map : distanceBetweenSchoolsAndPupils.entrySet()) {
            //b -> u ... u -> s
            List<Pupil> listPupil = getPupilFromTheSameSchool(map.getValue());
            Permutation permutation = new Permutation(listPupil, listPupil.size());
            List<List<Pupil>> permutations = permutation.getPermutation();
            Route route = getMinimalRouteForSchool(permutations, busDistance, map);
            route.setBus(bus);
            minRoute = (minRoute.getDistance() > route.getDistance() ? route : minRoute);
        }
        minimalRoute.add(minRoute);
    }

    private List<Pupil> getPupilFromTheSameSchool(List<SchoolDistance> value) {
        List<Pupil> pupils = new ArrayList<>();
        for (SchoolDistance schoolDistance : value) {
            pupils.add(schoolDistance.getPupil());
        }
        return pupils;
    }

    private Route getMinimalRouteForSchool(List<List<Pupil>> routes, List<BusDistance> busDistance,Map.Entry<School, List<SchoolDistance>> schoolDistance) {
        int minDistance = Integer.MAX_VALUE;
        List<Pupil> minRoute = null;

        for (List<Pupil> route : routes) {
            int tempDistance = countDistanceInRoute(route, busDistance, schoolDistance);
            if (minDistance > tempDistance) {
                minDistance = tempDistance;
                minRoute = route;
            }
        }
        return new Route(minDistance, minRoute, schoolDistance.getKey());

    }

    private int countDistanceInRoute(List<Pupil> route, List<BusDistance> busDistance, Map.Entry<School, List<SchoolDistance>> schoolDistance) {
        int distanceBetweenPupils = countDistanceBetweenPupils(route);
        int distanceBetweenBusAndPupil = countDistanceBetweenBusAndPupil(route.get(0), busDistance);
        int distanceBetweenPupilAndSchool = countDistanceBetweenSchoolAndPupil(route.get(route.size() - 1), schoolDistance.getValue());

        return  distanceBetweenPupils + distanceBetweenBusAndPupil + distanceBetweenPupilAndSchool;
    }

    private int countDistanceBetweenSchoolAndPupil(Pupil pupil, List<SchoolDistance> schoolDistances) {
        for (SchoolDistance schoolDistance : schoolDistances) {
            if (schoolDistance.getPupil().equals(pupil))
                return schoolDistance.getDistance();
        }
        return 0;
    }

    private int countDistanceBetweenBusAndPupil(Pupil pupil, List<BusDistance> busDistances) {
        for (BusDistance busDistance : busDistances) {
            if (busDistance.getPupil().equals(pupil))
                return busDistance.getDistance();
        }
        return 0;
    }


    private int countDistanceBetweenPupils(List<Pupil> route) {

        int distance = 0;
        for(int i = 0; i < route.size() - 1; i++) {
            distance+=countDistanceBetweenTwoPupils(route.get(i), route.get(i + 1));
        }
        return distance;
    }

    private int countDistanceBetweenTwoPupils(Pupil pupil, Pupil pupil2) {
        for (Map.Entry<Pupil, List<PupilDistance>> map : distanceBetweenPupil.entrySet()) {
            if (map.getKey().equals(pupil)) {
                for (PupilDistance pupilDistance : map.getValue()) {
                    if (pupilDistance.getPupil().equals(pupil2)) {
                        return pupilDistance.getDistance();
                    }
                }
            }
        }
        return 0;
    }

    private void printMinimalRoute() {

        for (Route route : globalRoute) {
            System.out.println("DISTANCE = " + route.getDistance() + " " + route.getBus() +  " " + (route.getSchool()));
            for (Pupil pupil : route.getBusRoute()) {
                System.out.println(pupil);
            }
            System.out.println();
        }
    }
}
