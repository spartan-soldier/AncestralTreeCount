package com.Bitgo.AncestralTreeCount.service;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static java.lang.Thread.sleep;

@Service
@Slf4j
public class MainService {
    RestTemplate restTemplate = new RestTemplate();

    public List<Pair> getNLargestAncestrySet(int n){
        Set<String> txns = new HashSet<>(Arrays.asList(getBlockTransactions("680000")));
        Map<String,Integer>  countMap = new HashMap<>();
        PriorityQueue<Pair<String,Integer>> minHeap = new PriorityQueue<>(Comparator.comparingInt(Pair::getValue));
        Map<String,List<String>> relationMap = createRelationMap(txns);
        for(String s: txns){
            if(relationMap.containsKey(s) && !countMap.containsKey(s)){
                dfs(countMap,relationMap,minHeap,s,n);
            }
        }

        // Just formatting the output to show it in sorted manner
        List<Pair> largestCountTxns = new ArrayList<>();
        while(!minHeap.isEmpty()){
            Pair<String,Integer> p = minHeap.poll();
            largestCountTxns.add(p);
        }
        System.out.println(largestCountTxns);

        return largestCountTxns;
    }
    

    public String getBlockHash(String blockId){
        final String uri = "https://blockstream.info/api/block-height/"+blockId;
        String result = restTemplate.getForObject(uri, String.class);
        System.out.println(result);
        return  result;
    }
    public String[] getBlockTransactions(String blockId){
        String blockHash = getBlockHash(blockId);
        final String uri = "https://blockstream.info/api/block/"+blockHash+"/txids";
        String[] result = restTemplate.getForObject(uri, String[].class);
        System.out.println(result.toString());
        return  result;
    }
    
    public JsonNode getTransactionForTxnId(String id){
        final String uri = "https://blockstream.info/api/tx/"+id+"?someparam="+id;
        JsonNode result = restTemplate.getForObject(uri, JsonNode.class);
        return  result;
    }

//    Map of child -> List of first level parents
    public Map<String,List<String>> createRelationMap(Set<String> validKeySet){

        Map<String,List<String>> relationMap = new HashMap<>();
        int count =1;
        for(String childTxnId: validKeySet){
            if(!relationMap.containsKey(childTxnId)){
                createRelationshipMapHelper(validKeySet,relationMap,childTxnId);
            }
            System.out.println(count);

            if(++count%200==0) {
//               break;
            }
        }

        return relationMap;
    }

    public void createRelationshipMapHelper(Set<String> validKeySet, Map<String,List<String>> relationMap,String childTxnId){
        JsonNode txnJson = getTransactionForTxnId(childTxnId);
        JsonNode parentTxns = txnJson.get("vin");
        for(JsonNode parentTxnJson: parentTxns){
            String parentTxnId =  parentTxnJson.get("txid").asText();
            if(validKeySet.contains(parentTxnId)){
                List<String> parentList = relationMap.getOrDefault(childTxnId,new ArrayList<>());
                parentList.add(parentTxnId);
                relationMap.put(childTxnId,parentList);
                //recursive call for the parent now
                createRelationshipMapHelper(validKeySet,relationMap,parentTxnId);
            }
        }
    }



    private int dfs(Map<String, Integer> countMap, Map<String, List<String>> relationMap,PriorityQueue<Pair<String,Integer>> pq, String s,int n) {
            if(countMap.containsKey(s))
                return countMap.get(s);
            if(!relationMap.containsKey(s)){
                if(pq.size()<n)
                    pq.add(new Pair(s,1));
                countMap.put(s,1);
                return 1;
            }
            else {
                int max =Integer.MIN_VALUE;
                for(String parent : relationMap.get(s)){
                    max = Math.max(max,1+ dfs(countMap,relationMap,pq,parent,n));
                }
                if(pq.size()==n && max>pq.peek().getValue()) {
                    pq.poll();
                    pq.add(new Pair(s,max));
                }
                else if(pq.size()<n) {
                    pq.add(new Pair(s, max));
                }
                countMap.put(s,max);
                return max;
            }
    }

   

    
    
    public class Pair<K,V>{
        K key;
        V value;

        public Pair(K key,V value){
            this.key = key;
            this.value = value;
        }
        public K getKey(){
            return this.key;
        }
        public V getValue(){
            return this.value;
        }
        @Override
        public String toString(){
            return "{ txnId: "+ key+", count: "+value +" }";
        }


    }


}
